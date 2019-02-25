(ns re-integrant.module.router
  (:require [integrant.core :as ig]
            [re-frame.core :as re-frame]
            [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [re-integrant.core :as re-integrant :refer-macros [defevent defsub deffx]]))

(defn- setup-router [routes]
  (letfn [(dispatch-route [{:keys [:handler :route-params]}]
            (let [panel-name (keyword (str (name handler) "-panel"))]
              (re-frame/dispatch [::set-active-panel panel-name route-params])))
          (parse-url [url]
            (when (empty? url)
              (set! js/window.location (str js/location.pathname "#/")))
            (let [url (-> url
                          (clojure.string/split #"&")
                          (first))]
              (bidi/match-route routes url)))]
    (let [history (pushy/pushy dispatch-route parse-url)]
      (.setUseFragment (aget history "history") true)
      (pushy/start! history)
      {:history history :routes routes})))

(defn- go-to-page [{:keys [history routes]} route]
  (pushy/set-token! history (apply bidi/path-for (cons routes route))))

;; Initial DB
(def initial-db {::active-panel :none ::router nil})

;; Subscriptions
(defmulti reg-sub identity)
(defsub reg-sub ::active-panel #(::active-panel %))
(defsub reg-sub ::route-params #(::route-params %))

;; Events
(defmulti reg-event identity)
(defevent reg-event ::init
  (fn [db [_ routes]]
    (-> db
        (merge initial-db)
        (assoc ::router (setup-router routes)))))
(defevent reg-event ::halt
  (fn [{{:keys [history]} ::router :as db} _]
    (pushy/stop! history)
    (->> db
         (remove #(= (namespace (key %)) (namespace ::x)))
         (into {}))))
(defevent reg-event ::go-to-page
  (fn [db [_ [route]]]
    (let [{:keys [::router]} db]
      (go-to-page router route))
    db))
(defevent reg-event ::set-active-panel
  (fn [db [_ panel-name route-params]]
    (assoc db
           ::active-panel panel-name
           ::route-params route-params)))

(defmethod ig/init-key :re-integrant.module/router [k {:keys [:routes]}]
  (js/console.log (str "Initializing " k))
  (re-integrant/init-module
   {:reg-sub reg-sub :reg-event reg-event
    :init-event [::init routes] :halt-event [::halt]}))

(defmethod ig/halt-key! :re-integrant.module/router [k module]
  (js/console.log (str "Halting " k))
  (re-integrant/halt-module module))

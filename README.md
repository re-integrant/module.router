# re-integrant/module.router

[re-integrant](https://github.com/re-integrant/core) module for SPA routing.

## Installation
To install, add the following to your project :dependencies:

```clojure
[re-integrant "0.1.0-SNAPSHOT"]
[re-integrant/module.router "0.1.0-SNAPSHOT"]
```

## Usage

```clojure:config.cljs
(require '[integrant.core :as ig]
         '[re-frame.core :as re-frame]
         '[re-integrant.module.router :as router])

(ig/init {:re-integrant.module/router
          {:routes ["/"     :home
                    "about" :about}]}})

@(re-frame/subscribe [::router/active-panel])
;; => :home-panel
```

## License

Copyright © 2019 Kazuki Tsutsumi

Distributed under the MIT license.
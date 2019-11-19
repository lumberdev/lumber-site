(ns shadow
  (:require [shadow.cljs.devtools.api :as shadow.api]
            [shadow.cljs.devtools.server :as shadow.server]
            [lumber.serve :refer [server]]
            [mount.core :as mount :refer [defstate]]))

(defstate shadow-web
  :start (do (shadow.server/start!)
             (shadow.api/watch :app))
  :stop (shadow.api/stop-worker :app))

(comment
  (mount/start)
  (mount/start #'lumber.serve/server)

  (mount/stop)
  (mount/start #'lumber.serve/server)

  (shadow.api/repl :app)
  :cljs/quit

  (shadow.server/stop!)
  )

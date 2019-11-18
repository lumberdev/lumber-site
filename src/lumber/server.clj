(ns lumber.server
  (:require
   [aleph.http :as http]
   [manifold.stream :as s]
   [uix.dom.alpha :as uix.dom]

   [lumber.core.home :as home]
   ))



(defn index-response
  []
  (let [res (s/stream)]
    (future
      (s/put! res head)
      (s/put! res (uix.dom/render-to-string [home]))
      (s/put! res end)
      (s/close! res))
    {:status 200
     :headers {"content-type" "text/html"}
     :body res}))

(defn handler [req]
  (case (:uri req)
    "/" (index-response)
    {:status 404
     :headers {"content-type" "text/plain"}
     :body "404"}))

(defn -main []
  (http/start-server #'handler {:port 8081}))

(comment
  (def server
    (http/start-server #'handler {:port 8081}))

  (defn stop! []
    (.close server)))

(ns lumber.serve
  (:require
   [aleph.http :as http]
   [mount.core :as mount :refer [defstate]]
   [manifold.stream :as s]
   [uix.dom.alpha :as uix.dom]

   [ring.middleware.resource :refer [wrap-resource]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.not-modified :refer [wrap-not-modified]]

   [lumber.home :as home]
   ))

(defn html []
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1"}]
    [:title "Lumber"]]
   [:link {:rel "stylesheet"
           :href "./css/main.css"
           :type "text/css"}]
   [:body
    [:div#app]
    [:script {:src "./js/main.js"}]
    [:script {:type "text/javascript"} "lumber.core.init();"]
    ]])

(defn index
  [req]
  (let [res (s/stream)]
    (future
      (uix.dom/render-to-stream [html] {:on-chunk #(s/put! res %)})
      (s/close! res))
    {:status  200
     :headers {"content-type" "text/html"}
     :body    res}))

(defn dev-handler [req]
  {:status  200
   :headers {"content-type" "text/html"}
   :body    (uix.dom/render-to-string [html])})

(def resource-handler
  (-> (constantly {:status 200})
      (wrap-resource "public")
      (wrap-content-type)
      (wrap-not-modified)))

(defn default-middleware [handler]
  (-> handler
      (wrap-params)
      (wrap-defaults site-defaults)))

(defn routes [{:as req :keys [uri]}]
  (case uri
    "/" (index req)
    resource-handler
    #_{:status 404
     :headers {"content-type" "text/plain"}
     :body "404"}))

(def handler (default-middleware routes))

(defstate server
  :start (http/start-server handler {:port 3400})
  :stop  (.close server))

(defn -main []
  (println "Starting Lumber ...")
  (mount/start)
  (print "Lumber is ready!"))

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
   ))

(defn raw-html
  ([s] (raw-html :div nil s))
  ([tag s] (raw-html tag nil s))
  ([tag attrs s]
   [tag (assoc attrs :dangerouslySetInnerHTML {:__html s})]))

(defn html []
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:title "Lumber"]
    [:link {:rel "stylesheet" :href "./css/main.css" :type "text/css"}]

    [:link {:rel "apple-touch-icon" :sizes "57x57" :href "/icons/apple-icon-57x57.png"}]
    [:link {:rel "apple-touch-icon" :sizes "60x60" :href "/icons/apple-icon-60x60.png"}]
    [:link {:rel "apple-touch-icon" :sizes="72x72" :href "/icons/apple-icon-72x72.png"}]
    [:link {:rel "apple-touch-icon" :sizes="76x76" :href "/icons/apple-icon-76x76.png"}]
    [:link {:rel "apple-touch-icon" :sizes="114x114" :href "/icons/apple-icon-114x114.png"}]
    [:link {:rel "apple-touch-icon" :sizes="120x120" :href "/icons/apple-icon-120x120.png"}]
    [:link {:rel "apple-touch-icon" :sizes="144x144" :href "/icons/apple-icon-144x144.png"}]
    [:link {:rel "apple-touch-icon" :sizes="152x152" :href "/icons/apple-icon-152x152.png"}]
    [:link {:rel "apple-touch-icon" :sizes="180x180" :href "/icons/apple-icon-180x180.png"}]
    [:link {:rel "icon" :type "image/png" :sizes "192x192" :href "/icons/android-icon-192x192.png"}]
    [:link {:rel "icon" :type "image/png" :sizes "32x32" :href "/icons/favicon-32x32.png"}]
    [:link {:rel "icon" :type "image/png" :sizes "96x96" :href "/icons/favicon-96x96.png"}]
    [:link {:rel "icon" :type "image/png" :sizes "16x16" :href "/icons/favicon-16x16.png"}]
    [:link {:rel "manifest" :href "/icons/manifest.json"}]
    [:meta {:name "msapplication-TileColor" :content "#ffffff"}]
    [:meta {:name "msapplication-TileImage" :content "/icons/ms-icon-144x144.png"}]
    [:meta {:name "theme-color" :content "#ffffff"}]
    [:script {:src "https://www.googletagmanager.com/gtag/js?id=UA-125582926-1" :async "true"}]
    [:script {:dangerouslySetInnerHTML
              {:__html "window.dataLayer = window.dataLayer || [];
                        function gtag () { dataLayer.push(arguments); }
                        gtag('js', new Date());
                        gtag('config', 'UA-125582926-1');"}}]]

   [:body
    [:div#app]
    [:script {:src "/js/compiled/main.js"}]
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


;; without router
(def app-resource-handler
  (-> (constantly {:status 200})
      (wrap-resource "public")
      (wrap-content-type)
      (wrap-not-modified)))

(defn app-default-middleware [handler]
  (-> handler
      (wrap-params)
      (wrap-defaults (assoc site-defaults :static {:resources "public"}))))

(defn app-routes [{:as req :keys [uri]}]
  (case uri
    "/" (index req)
    {:status 404
     :headers {"content-type" "text/plain"}
     :body "404"}))

(def app-handler (app-default-middleware app-routes))

(defstate server
  ;; :start (http/start-server app-handler {:port 80})
  :start (http/start-server #'app-handler {:port 3600})
  :stop  (.close server))

(defn -main []
  (println "Starting Lumber ...")
  (mount/start)
  (print "Lumber is ready!"))

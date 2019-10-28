(ns lumber.core
  (:require [uix.dom.alpha :as uix.dom]))

(defn start []
  (uix.dom/hydrate
   [:div [:h1 "We are Lumber"]]
   (.getElementById js/document "root")))

(defn ^:export init []
;; init is called ONCE when the page loads
;; this is called in the index.html and must be exported
;; so it is available even in :advanced release builds
  (start))

(defn stop []
;; stop is called before any code is reloaded
;; this is controlled by :before-load in the config
  (js/console.log "stop"))

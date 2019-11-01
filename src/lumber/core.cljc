(ns lumber.core
  (:require
   [uix.core.alpha :as uix]
   [uix.dom.alpha :as uix.dom]
   ))

;; Example:
;; (defn button [{:keys [on-click]} text]
;;   [:button.btn {:on-click on-click}
;;    text])
;; (defn app []
;;   (let [state* (uix/state 0)]
;;     [:<>
;;      [button {:on-click #(swap! state* dec)} "-"]
;;      [:span @state*]
;;      [button {:on-click #(swap! state* inc)} "+"]]))
;; [app]

(defn nav []
  [:header
   [:nav "Lumber"]]
  )

(defn home []
  [:div.cont
   [nav]
   ])

(defn start []
  (prn "start")
  (uix.dom/hydrate
   [home]
   (.getElementById js/document "app")))

(defn ^:export init []
;; init is called ONCE when the page loads
;; this is called in the index.html and must be exported
;; so it is available even in :advanced release builds
  (start))

(defn stop []
;; stop is called before any code is reloaded
;; this is controlled by :before-load in the config
  (prn "stop"))

(defn reload [] (prn "reload"))

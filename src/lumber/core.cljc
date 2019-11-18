(ns lumber.core
  (:require
   [uix.dom.alpha :as uix.dom]
   [uix.core.alpha :as uix]
   [xframe.core.alpha :as xf :refer [<sub]]

   [lumber.home :as home]

   #?(:cljs [cljs-bean.core :as bean])
   ))

(xf/reg-event-db
 :db/init
 (fn [_ _]
   {:partners
    ["Casper" "Resy" "Choosy" "Patagonia" "Workframe" "Village Studio"]
    :projects
    {:vimsical {:video "https://www.youtube.com/watch?v=OtvK24bG_IY&feature=youtu.be"
                :code  "https://github.com/vimsical/vimsical"}
     :juergen  {:code  "https://github.com/lumberdev/Juergen"}}
    :open false
    :scroll 0
    :embed ""
    }))

(defn percentage [total x] (/ (* 100.0 x) total))

(defn scroll-percentage []
  (percentage (- js/document.documentElement.scrollHeight
                 js/document.documentElement.clientHeight)
              js/document.documentElement.scrollTop))

(xf/reg-event-db :set-scroll
                 (fn [db [_ value]]
                   (assoc-in db [:scroll] (scroll-percentage))))

(xf/reg-event-db :open
                 (fn [db [_ value]]
                   (-> db
                       (assoc-in [:embed] value)
                       (assoc-in [:open] true))))

(xf/reg-event-db :close
                 (fn [db [_ value]]
                   (assoc-in db [:open] false)))

(xf/reg-sub :db/scroll   (fn [] (:scroll   (xf/<- [::xf/db]))))
(xf/reg-sub :db/partners (fn [] (:partners (xf/<- [::xf/db]))))
(xf/reg-sub :db/open     (fn [] (:open     (xf/<- [::xf/db]))))
(xf/reg-sub :db/embed    (fn [] (:embed    (xf/<- [::xf/db]))))

(defn start []
  (prn "start")

  (defonce init-db (xf/dispatch [:db/init]))

  (js/window.addEventListener "scroll" (fn [e] (xf/dispatch [:set-scroll])))
  (js/setTimeout (fn [] (prn "ready!")) 0)

  (uix.dom/hydrate
   [home/home]
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

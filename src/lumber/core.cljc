(ns lumber.core
  (:require
   [uix.dom.alpha :as uix.dom]
   [uix.core.alpha :as uix]
   [xframe.core.alpha :as xf :refer [<sub]]

   [lumber.home :as home]
   [lumber.ui :as ui]

   #?(:cljs [goog.events :as events])
   #?(:cljs [goog.events.EventType :as EventType])

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
    :scroll-dir :down
    :resize {}
    :eyes-pos {:x 0 :y 0}
    :mouse {:x 0 :y 0}
    :mouse-stop true
    :variant :roll
    :blink 7
    :embed ""
    :gravity 9.81
    }))

(defn mouse-move-handler [e]
  (do (xf/dispatch [:set-mouse-stop false])
      (xf/dispatch [:set-variant :follow])
      (xf/dispatch [:set-mouse e])))

(defn mouse-stop-handler [e]
  (do (xf/dispatch [:set-variant :roll])
      (xf/dispatch [:set-mouse-stop true])))

(defn scroll-handler [e]
  (do (xf/dispatch [:set-scroll (.-scrollY js/window)])))

(defn resize-handler [e]
  (do (xf/dispatch [:set-resize e])))

(def debounced-mouse-move-handler (goog.functions.debounce mouse-move-handler 250))
(def debounced-mouse-stop-handler (goog.functions.debounce mouse-stop-handler 1000))
(def debounced-scroll-handler     (goog.functions.debounce scroll-handler 60))
(def debounced-resize-handler     (goog.functions.debounce resize-handler 250))

(xf/reg-event-db :set-scroll
                 (fn [db [_ value]]
                   (let [prev (:y (get-in db [:scroll]))
                         dy   (- value prev)]
                     (do
                      (assoc-in db [:scroll]
                                {:y value
                                 :dy dy
                                 :eyes (ui/measure-eyes)
                                 :header (ui/measure-header)}
                                )))))


(xf/reg-event-db :set-scroll-dir
                 (fn [db [_ value]]
                   (assoc-in db [:scroll-dir] value)))

(xf/reg-event-db :set-mouse
                 (fn [db [_ value]]
                   (assoc-in db [:mouse] value)))

(xf/reg-event-db :set-mouse-stop
                 (fn [db [_ v]]
                   (assoc-in db [:mouse-stop] v)))

(xf/reg-event-db :set-resize
                 (fn [db [_ value]]
                   (assoc-in db [:set-resize] value)))

(xf/reg-event-db :set-gravity
                 (fn [db [_ value]]
                   (assoc-in db [:gravity] value)))

(xf/reg-event-db :set-variant
                 (fn [db [_ value]]
                   (assoc-in db [:variant] value)))

(xf/reg-event-db :set-blink
                 (fn [db [_ value]]
                   (assoc-in db [:blink] value)))

(xf/reg-event-db :open (fn [db [_ value]]
                   (-> db
                       (assoc-in [:embed] value)
                       (assoc-in [:open] true))))

(xf/reg-event-db :close
                 (fn [db [_ value]]
                   (assoc-in db [:open] false)))

(xf/reg-sub :db/scroll     (fn [] (:scroll     (xf/<- [::xf/db]))))
(xf/reg-sub :db/scroll-dir (fn [] (:scroll-dir (xf/<- [::xf/db]))))
(xf/reg-sub :db/mouse      (fn [] (:mouse      (xf/<- [::xf/db]))))
(xf/reg-sub :db/mouse-stop (fn [] (:mouse-stop (xf/<- [::xf/db]))))
(xf/reg-sub :db/resize     (fn [] (:resize     (xf/<- [::xf/db]))))
(xf/reg-sub :db/gravity    (fn [] (:gravity    (xf/<- [::xf/db]))))
(xf/reg-sub :db/variant    (fn [] (:variant    (xf/<- [::xf/db]))))
(xf/reg-sub :db/blink      (fn [] (:blink      (xf/<- [::xf/db]))))
(xf/reg-sub :db/partners   (fn [] (:partners   (xf/<- [::xf/db]))))
(xf/reg-sub :db/open       (fn [] (:open       (xf/<- [::xf/db]))))
(xf/reg-sub :db/embed      (fn [] (:embed      (xf/<- [::xf/db]))))

(defn start []
  (prn "start")

  (defonce init-db (xf/dispatch [:db/init]))

  (uix.dom/hydrate
   [home/home]
   (.getElementById js/document "app"))

  (js/window.addEventListener "mousemove" mouse-move-handler)
  (js/window.addEventListener "scroll"    scroll-handler)
  (js/window.addEventListener "resize"    debounced-resize-handler)
  ;; (js/window.addEventListener "orientationchange" resize-handler)
  ;; (js/window.addEventListener "deviceorientation" resize-handler)
  )

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

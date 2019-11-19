(ns lumber.ui
  (:require
   [uix.dom.alpha :as uix.dom]
   [uix.core.alpha :as uix]
   [xframe.core.alpha :as xf :refer [<sub]]
   #?(:cljs ["framer-motion" :refer (motion useMotionValue useViewportScroll)])
   ))

;;;;
;; Progress
;;;;
(defn percentage [total x] (/ (* 100.0 x) total))

(defn scroll []
  #?(:cljs (percentage (- js/document.documentElement.scrollHeight
                          js/document.documentElement.clientHeight)
                       js/document.scrollingElement.scrollTop)))

(defn progress []
  #?(:cljs (let [scroll (<sub [:db/scroll])]
             [:div.progress
              [:> (.-div motion)
               {:class "state" :style {:width (str scroll "%")}}]
              [:div.rail]])))

;;;;
;; Popup
;;;;
(defn video-size [screen-width screen-height ratio margin]
  (cond (> screen-width 1200)
        {:width 662 :height 414}
        (> screen-width screen-height)
        {:width screen-height :height (/ screen-height ratio)}
        :else {:width (- screen-width (* 2 margin))
               :height (/ (- screen-width (* 2 margin)) ratio)}))

(defn popup []
  #?(:cljs (let [open?         (<sub [:db/open])
                 src           (<sub [:db/embed])
                 screen-height js/document.documentElement.clientHeight
                 screen-width  js/document.documentElement.clientWidth
                 ratio         (/ 662 414)
                 size          (video-size screen-width screen-height ratio 20)
                 close-right   (if (< screen-width 800) (- (/ (:width size) 2) 20) -40)]
             [:div.popup {:on-click #(xf/dispatch [:close])
                          :style {:display (if open? "grid" "none")
                                  :height (str  "px")}}
              [:article
               [:div.btn-x {:on-click #(xf/dispatch [:close])
                            :style {:right close-right}}
                [:div.x.l]
                [:div.x.r]]
               [:iframe {:width (str (:width size))
                         :height (str (:height size))
                         :src src
                         :frame-border "0"
                         :fs "1"
                         :allow-full-screen (clj->js true)
                         :allow "accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture;"}]]])))



;;;;
;; Clock
;;;;
(defn time->angles [hours mins secs]
  {:hours (+ (* 30 hours) (/ mins 2))
   :mins (* 6 mins)
   :secs (* 6 secs)})

(defn clock-arrow
  "Takes:
  - start angle int?,
  - duration time in seconds int?,
  - css classes string?"
  [start duration cls]
  #?(:cljs [:> (.-div motion)
            {:class (str "clock-arrow " cls)
             :animate #js {:rotate #js [start (+ start 360)]}
             :transition #js {:duration duration
                              :ease "linear"
                              :loop (clj->js 'Infinity)}}]))

(defn clock
  "Takes a utc timezone int?
  Example for Sofia utc+2: [clock 2]"
  [utc]
  #?(:cljs (let [date (new js/Date)
                 hours  (+ (.getUTCHours ^js date) utc)
                 mins   (.getUTCMinutes ^js date)
                 secs   (.getUTCMinutes ^js date)
                 angles (uix/state (time->angles hours mins secs))]
             [:div.clock-cont
              [:div.clock-bg.white-bg
               [:div.arrows-intersection.yellow-bg]
               [clock-arrow (:hours @angles) (* 12 60 60 1) "hour yellow-bg re"] ;; 43200s
               [clock-arrow (:mins  @angles) (* 60 60 1)    "min yellow-bg re"]  ;; 3600s
               [clock-arrow (:secs  @angles) (* 60 1)       "sec gray-bg re"]    ;; 60s
               ]])))

(ns lumber.dots
  (:require
   [clojure.string :as s]

   [uix.dom.alpha :as uix.dom]
   [uix.core.alpha :as uix]
   [xframe.core.alpha :as xf :refer [<sub]]

   #?(:cljs [goog.functions])

   #?(:cljs ["react-anime" :default anime])
   #?(:cljs ["framer-motion" :refer
             (motion transform useMotionValue
              useViewportScroll useSpring
              useCycle useAnimation)])
   ))


(def black "#000")
(def yellow "#FFCC08")
(defn sec [n] (str n "s"))
(defn per [n] (str n "%"))
(defn log [x]
  #?(:cljs (js/console.log x))
  #?(:clj  (prn x)))

(defn rand-r
  "Random number in range [min..max]."
  [min max]
  (+ (* (rand) (- max min)) min))

(defn transform-range
  "Transfroms value from input togoutput range.
   R_in = [0 100] R_out = [0 1] v = 50 results in 0.5
   Example: (transform-range 0 100 0 10 50) ;; -> 0.5"
  [in-min in-max out-min out-max value]
  (let [in (- in-max in-min)
        out (- out-max in-min)]
    (+ out-min (/ (* out (- value in-min)) in))))

(defn accumulate
  "Accumulative Sum of xs.
  Example: (accumlate [1 2 3 4]) -> [1 3 6 10]"
  [xs]
  (reduce
   (fn [acc n] (conj acc (reduce + 0 (take n xs))))
   []
   (range 1 (inc (count xs)))))

(defn bounding-rect-elem [elem]
  (let [r (-> elem .getBoundingClientRect)]
    {:left (.-left r) :top (.-top r) :width (.-width r) :height (.-height r)}))

(defn bounding-rect [e]
  (let [r (-> e .-target .getBoundingClientRect)]
    {:left (.-left r) :top (.-top r) :width (.-width r) :height (.-height r)}))

(defn client-pos [e]
  {:x (-> e .-clientX) :y (-> e .-clientY)})

(defn screen-pos [e]
  {:x (-> e .-screenX) :y (-> e .-screenY)})

(defn relative-pos [rect global]
  {:x (- (:x global) (:left rect))
   :y (- (:y global) (:top rect))})

(defn mobile-device? []
  #?(:cljs
     (let [user-agent (s/lower-case (-> js/window .-navigator .-userAgent))
           rxp        #"android|webos|iphone|ipad|ipod|blackberry|iemobile|opera mini"]
       (boolean (re-find rxp user-agent)))))

(defn use-window-event
  "Window event hook (it cleans after itself).
  Example: (use-window-event 'mouse' mouse-handler)"
  [event cb]
  (uix/effect! (fn []
                 (js/window.addEventListener event cb)
                 (fn [] (js/window.removeEventListener event cb))
                 [event cb])))

(defn use-global-mouse-move [cb]
  (use-window-event "mouse-move" cb))

;;;;
;; Glow Mouse on Hover Dots
;;;;
(defn glow-dots []
  #?(:cljs (let [rect     (uix/ref   {:width 300 :height 300 :left 40 :top 240})
                 pos      (uix/state {:x 150 :y 150})
                 visible  (uix/state false)
                 cont     (uix/ref)
                 set-cont (uix/with-effect (reset! rect (bounding-rect-elem @cont)))
                 r 0
                 set-position (fn [e]
                               (let [mouse    (client-pos e)
                                     relative (relative-pos @rect mouse)]
                                 (reset! pos {:x (:x relative) :y (:y relative)})))
                 mobile? (mobile-device?)]
             [:div.glow-cont.cf
              {:ref cont
               :onMouseEnter (fn [e]
                               (do (set-position e)
                                   (reset! visible (not @visible))))
               :onMouseLeave (fn [] (reset! visible (not @visible)))
               :onMouseMove  set-position
               :onTap(fn [e]
                       (do (set-position e)
                           (reset! visible (not @visible))))
               }
              [:> (.-div motion)
               (merge
                (if mobile?
                  {:draggable true
                   :drag (clj->js true)
                   :dragElastic 0
                   :dragConstraints
                   (clj->js
                    {:top 40
                     :left 40
                     :right (:width @rect)
                     :bottom (:height @rect)})
                   } {})
                {:class "glow yellow-gr"
                 :style
                 {:opacity (if @visible 1 0)
                  :transform (str "translate(" (:x @pos) "px," (:y @pos) "px)")
                  :transition "opacity 0.8s ease-in"}})

               [:div.wave.wave-1.yellow-gr]
               [:div.wave.wave-2.yellow-gr]
               [:div.wave.wave-3.yellow-gr]
               ]
              (for [i (range 0 25)] ^{:key i}
                [:div.dot-cont
                 [:div {:class "dot black-bg"}]])])))



;;;;
;; Grid Hover Dots
;;;;
(defn grid-hover [i]
  (let [left-edge  #{0 5 10 15 20}
        right-edge #{4 9 14 19 24}]
    (cond (contains? left-edge i)
          [(- i 5) (- i 4)
              i    (inc i)
           (+ i 5) (+ i 6)]
          (contains? right-edge i)
          [(- i 6) (- i 5)
           (dec i)    i
           (+ i 4) (+ i 5)]
          :else
          [(- i 6) (- i 5) (- i 4)
           (dec i)    i    (inc i)
           (+ i 4) (+ i 5) (+ i 6)]
          )))

(defn update-hover-state [state index-inner scale-outer scale-inner]
  (merge @state (reduce (fn [acc j]
                          (if (= j index-inner)
                            (assoc acc j scale-inner)
                            (assoc acc j scale-outer)))
                        {} (grid-hover index-inner))))

(defn grid-dots []
  #?(:cljs(let [s (uix/state {})
                p (uix/state 0)
                done (uix/state {})
                duration 0.25]
            [:div.cf {:style {:width "100%"}}
             (for [i (range 0 25)] ^{:key i}
               [:> (.-div motion)
                {:class "dot-cont"
                 :onHoverStart
                 (fn [] (do (reset! s (update-hover-state s i 0.65 0.3))))
                 :onHoverEnd
                 (fn [] (do
                          (reset! s (update-hover-state s i 1 1))))
                 :onTap
                 (fn [] (do (reset! s (update-hover-state s @p 1 1))
                            (reset! s (update-hover-state s i 0.65 0.3))
                            (reset! p i)))
                 }
                [:> (.-div motion)
                 {:class "dot black-bg"
                  :animate #js {:scale (get @s i)}
                  :transition (clj->js {:type "spring" :mass 0.5})
                  }]])])))


;;;;
;; Flip on Hover Dots
;;;;
(defn flipping-dots
  ([] (flipping-dots 0.3 "ease-in-out" 0.2))
  ([duration easing delay]
   #?(:cljs (let [state          (uix/state {:hover false :done true})
                  total-duration (* 1000 (+ (* delay 5) duration))
                  index->delay   [5 4 3 4 5
                                  4 2 1 2 4
                                  3 1 0 1 3
                                  4 2 1 2 4
                                  5 4 3 4 5]]
              [:> (.-div motion)
               {:class "flipping-dots cf"
                :onHoverStart
                (fn [] (do (reset! state {:hover true :done false})
                           (js/setTimeout #(swap! state assoc :done true) total-duration)))
                :onHoverEnd
                (fn [] (do (swap! state assoc :hover false)))

                :onTapStart
                (fn [] (do (reset! state {:hover true :done false})
                         (js/setTimeout #(reset! state {:hover false :done true}) total-duration)))
                }
               (for [i index->delay] ;;^{:key i}
                 [:div
                  {:class (str "flipper "
                               (if (and (:done @state)
                                        (not (:hover @state)))
                                 "show-back"
                                 "show-front"))
                   :style {:transition-timing-function easing
                           :transition-duration        (sec duration)
                           :transition-delay           (sec (* i delay))}}
                  [:div.dot.front.black-bg]
                  [:div.dot.back.yellow-bg]]
                 )]))))



;;;;
;; Rain on Col Hover Dots
;;;;
(defn raining-dots
  ([] (raining-dots 0.25 0.3))
  ([delay duration]
   #?(:cljs (let [s (uix/state {0 {:hover false :done true}
                                1 {:hover false :done true}
                                2 {:hover false :done true}
                                3 {:hover false :done true}
                                4 {:hover false :done true}})
                  col-duration  (* (+ delay (* 5 duration)) 1000)
                  variants #js
                  {:init #js {}
                   :rain #js {:backgroundColor #js [black yellow black]}}]
              [:div.cf {:style {:width "100%"}}
               (for [col [0 1 2 3 4]] ^{:key col}
                 [:> (.-div motion)
                  {:class "dot-cont"
                   :onHoverStart
                   (fn [_ _] (do (swap! s assoc-in [col] {:hover true :done false})
                                 (js/setTimeout #(swap! s assoc-in [col :done] true) col-duration)))

                   :onHoverEnd
                   (fn [_ _] (swap! s assoc-in [col :hover] false))

                   :onTapStart
                   (fn [_ _] (do (swap! s assoc-in [col] {:hover true :done false})
                                 (js/setTimeout #(swap! s assoc-in [col] {:done true :hover false}) col-duration)))
                   }
                  (for [i [0 1 2 3 4]] ^{:key i}
                    [:> (.-div motion)
                     {:class "dot black-bg"
                      :transition #js {:delay (* i delay)
                                       :duration duration
                                       :times #js [0 0.99 1]
                                       :repeatDelay 1500}
                      :animate (let [done?  (get-in @s [col :done])
                                     hover? (get-in @s [col :hover])]
                                 (if (and done? (not hover?))
                                   "init"
                                   "rain"
                                   ))
                      :variants variants
                      }])]
                 )]))))



;;;;
;; Follow Mouse on Hover Dots
;;;;
(defn rand-blink []
  (* 1 (rand-nth (range 3 10))))

(defn set-blink [e]
  (xf/dispatch [:set-blink (rand-blink)]))

(def debounced-set-blink (goog.functions.debounce set-blink 500))

(defn lookX [client mouse]
  (transform-range 0 (:w client) -100 190 (.-clientX mouse)))

(defn lookY [client mouse]
  (transform-range 0 (:h client) -40 80 (.-clientY mouse)))

(defn eye-dots [pos]
  #?(:cljs (let [client   {:w js/window.innerWidth :h js/window.innerHeight}
                 mouse    (<sub [:db/mouse])
                 blk      (uix/ref true)
                 x        (lookX client mouse)
                 y        (lookY client mouse)
                 variant  (<sub [:db/variant])
                 variants {:follow
                           {:style
                            {:transform (str "translate( " x "%," y "%)")
                             :transition "transform 0.15s ease-out"}
                            :onTransitionEnd
                            (fn [] (xf/dispatch [:set-variant :roll]))
                            }
                           :roll
                           {:style
                            {:transform (str "translate( " (rand-r -100 90) "%," (rand-r -40 40) "%)")
                             :transition "transform 0.15s ease-out 5s"
                             }
                            :onTransitionEnd
                            (fn []
                              (xf/dispatch [:set-variant :follow])
                              (xf/dispatch [:set-variant :roll]))}}]
             [:div.eyes-cont.cf
              (for [i (range 0 25)] ^{:key i}
                (do [:div.dot.black-bg
                     (when (contains? (into #{} pos) i)
                       [:div
                        [:div.eye
                         [:div {:class (if true "pupil blink" "pupil")}]]
                        [:div.eyeball (variant variants)]]
                       )]))])))



;;;;
;; Floating Dots
;;;;
(defn rand-edge [min-x max-x min-y max-y n]
  (let [edges [{:x min-x :y (rand-r min-y max-y)}
               {:x (rand-r min-y max-y) :y min-y}
               {:x max-x :y (rand-r min-y max-y)}
               {:x (rand-r min-y max-y) :y max-y}]
        orders [[0 1 2 3]
                [1 2 3 0]
                [2 3 0 1]
                [3 0 1 2]]
        start (rand-nth edges)
        order (rand-nth orders)
        idxs  (take n (cycle order))
        pos   (mapv (partial nth edges) idxs)]
    pos))

(defn floating-dots [n offsets]
  #?(:cljs [:svg {:viewBox "0 0 100 100"}
            (for [i (range 1 (inc n))] ^{:key i}
              (let [w 100
                    offset (rand-nth offsets)
                    r 5
                    size 4.5
                    x-min (- (+ 0 size) offset)
                    x-max (- (+ w offset) size)
                    y-min (- (+ 0 size) offset)
                    y-max (- (+ w offset) size)
                    x (rand-r x-min x-max)
                    y (rand-r y-min y-max)
                    xs (repeatedly 100 (fn [] {:value (str (rand-r x-min x-max) "%")
                                               :duration (rand-r 1000 2000)}))
                    ys (repeatedly 100 (fn [] {:value (str (rand-r y-min y-max) "%")
                                               :duration (rand-r 1000 2000)}))]
                [:> anime
                 {:cx (clj->js xs)
                  :cy (clj->js ys)
                  :loop true
                  :direction "alternate"
                  :easing "easeInOutSine"}
                 [:circle {:cx x :cy y :r (str r "%") :fill "#ffcc08"}]]
                )
              )]))

;;;;
;; Gravity
;;;;
(defn make-dot [position velocity]
  {:position position
   :velocity velocity
   :mass 1
   :radius 5
   :restitution -0.7})

(defn frontal-area [dot]
  (/ (* Math/PI (:radius dot) (:radius dot)) 10000))

(defn drag-force [dot d]
  (let [Cd 0.47
        rho 1.22
        A (frontal-area dot)
        Vd (-> dot :velocity d)]
    (if (zero? Vd) 0
        (/ (* (- 0 0.5) Cd A rho (* 3 Vd)) (Math/abs Vd)))))

(defn accel
  ([dot d] (accel dot d 0))
  ([dot d ag]
   (+ ag (/ (drag-force dot d) (:mass dot)))))

(defn position [o]
  (let [frame-rate (/ 1.0 120)
        ag 9.81
        ax (accel o :x 0)
        ay (accel o :y 9.81)
        vx (+ (-> o :velocity :x) (* ax frame-rate 1))
        vy (+ (-> o :velocity :y) (* ay frame-rate 1))
        px (+ (-> o :position :x) (* vx frame-rate 100))
        py (+ (-> o :position :y) (* vy frame-rate 100))]
    (make-dot {:x px :y py} {:x vx :y vy})))

(defn collision [width height dot]
  (let [px (-> dot :position :x)
        py (-> dot :position :y)
        vx (-> dot :velocity :x)
        vy (-> dot :velocity :y)
        r  (:radius dot)
        restitution (:restitution dot)]
      (cond (> py (- height r))
            (collision width height (make-dot {:x px :y (- height r)}
                                              {:x vx :y (* vy restitution)}))
            (< py 0)
            (collision width height (make-dot {:x px :y r}
                                              {:x vx :y (* vy restitution)}))
            (> px (- width r))
            (collision width height (make-dot {:x (- width r) :y py}
                                              {:x (* vx restitution) :y vy}))
            (< px r)
            (collision width height (make-dot {:x r :y py}
                                              {:x (* vx restitution) :y vy}))
         :else dot)))

(defn collision? [dot dot2]
  (let [px  (-> dot :position :x)
        py  (-> dot :position :y)
        px2 (-> dot2 :position :x)
        py2 (-> dot2 :position :y)
        r   (:radius dot)]
    (not (or (< (+ py r) py2)
             (> py (+ py2 r))
             (< (+ px r) px2)
             (> px (+ px2 r))))))

(def move (comp position (partial collision 100 100)))

(defn use-raf [cb]
  (let [id (uix/ref 0)]
    (uix/effect! (fn []
                  (reset! id (js/window.requestAnimationFrame cb))
                  (fn [] (js/window.cancelAnimationFrame id))
                  [cb]))))

(defn update-velocity [dot velocity]
  (make-dot (:position dot) velocity))

(defn update-velocities [dots velocities]
  (mapv (fn [d v] (update-velocity d v)) dots velocities))

(defn rand-update-velocities [size dots]
  (update-velocities dots (repeatedly size (fn [] {:x 0 :y (rand-r 3 8)}))))

(defn gravity-dots [n]
  #?(:cljs (let [w 100
                 offset 0
                 r 4.8
                 size 4.5
                 x-min  (- (+ 0 size) offset)
                 x-max  (- (+ w offset) size)
                 y-min  (- (+ 0 size) offset)
                 y-max  (- (+ w offset) size)
                 scroll (<sub [:db/scroll-dir])
                 init-dots (repeatedly 7 #(make-dot {:x (rand-r x-min x-max) :y y-min}
                                                    {:x 0 :y (rand-r 1 10)}))
                 dots (uix/state init-dots)
                 raf  (use-raf (fn [] (reset! dots (mapv move @dots))))
                 ]
             [:svg {:viewBox "0 0 100 100"
                    :onClick
                    (fn []
                      (reset! dots
                              (repeatedly 7 #(make-dot {:x (rand-r x-min x-max) :y y-min}
                                                       {:x 0 :y (rand-r 1 10)}))))

                    :onTouchEnd
                    (fn []
                      (reset! dots
                              (repeatedly 7 #(make-dot {:x (rand-r x-min x-max) :y y-min}
                                                       {:x 0 :y 5}))))
                    }
              (for [dot @dots]
                (let [done (uix/ref true)]
                   [:circle
                   {:cx (str (-> dot :position :x) "%")
                    :cy (str (-> dot :position :y) "%")
                    :r (str r "%")
                    :fill "#ffcc08"}]
                  ))])))

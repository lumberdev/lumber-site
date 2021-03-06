(ns lumber.dots
  (:require
   [clojure.string :as s]
   [uix.core.alpha :as uix]
   [xframe.core.alpha :as xf :refer [<sub]]
   #?(:cljs [goog.functions])
   #?(:cljs [react :as r])
   #?(:cljs [rooks :refer [useVisibilitySensor]])
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
  #?(:clj (prn x)))

(defn rand-r
  "Random number in range [min..max]."
  [min max]
  (+ (* (rand) (- max min)) min))

(defn transform-range
  "Transfroms value from input togoutput range.
   R_in = [0 100] R_out = [0 1] v = 50 results in 0.5
   Example: (transform-range 0 100 0 10 50) ;; -> 0.5"
  [a b c d value]
  (cond (> value b) d
        (< value a) c
        :else (+ c (* (- value a) (/ (- d c) (- b a))))))

(defn accumulate
  "Accumulative Sum of xs.
  Example: (accumlate [1 2 3 4]) -> [1 3 6 10]"
  [xs]
  (reduce
   (fn [acc n] (conj acc (reduce + 0 (take n xs))))
   []
   (range 1 (inc (count xs)))))

(defn bounding-rect [elem]
  #?(:cljs
     (let [r (-> elem .getBoundingClientRect)
           t (.-top r)
           d (- (+ (.-pageYOffset js/window) (.-scrollTop elem))
                (.-innerHeight js/window))
           ]
       {:top    (.-top r)
        :right  (.-right r)
        :bottom (.-bottom r)
        :left   (.-left r)
        :width  (.-width r)
        :height (.-height r)
        :doc    (+ t d)})))

(defn client-pos [e]
  {:x (-> e .-clientX) :y (-> e .-clientY)})

(defn screen-pos [e]
  {:x (-> e .-screenX) :y (-> e .-screenY)})

(defn page-pos [e]
  {:x (-> e .-pageX) :y (-> e .-pageY)})

(defn relative-pos [rect global]
  {:x (- (:x global) (:left rect))
   :y (- (:y global) (:top rect))})

(defn client-size []
  #?(:cljs
     {:width js/window.innerWidth :height js/window.innerHeight}))

(defn mobile-device? []
  #?(:cljs
     (let [user-agent (s/lower-case (-> js/window .-navigator .-userAgent))
           rxp        #"android|webos|iphone|ipad|ipod|blackberry|iemobile|opera mini"]
       (boolean (re-find rxp user-agent)))))

(defn in-view-port? [elem]
  (let [rect     (bounding-rect elem)
        client   (client-size)
        in-view? (and (<= (:top rect) (:height client))
                      (>= (+ (:top rect) (:height rect)) 0))]
    in-view?))

(defn use-window-events
  "Window event hook (it cleans after itself).
  Example: (use-window-event 'mouse' mouse-handler)"
  [event cb]
  #?(:cljs
     (uix/effect! (fn []
                    (js/window.addEventListener event cb)
                    (fn [] (js/window.removeEventListener event cb))
                    [event cb]))))

(defn use-global-mouse-move [cb]
  (use-window-events "mousemove" cb))

;;;;
;; Glow Mouse on Hover Dots
;;;;
(defn glow-dots []
  #?(:cljs
     (let [rect         (uix/ref {:width 300 :height 300 :left 40 :top 240})
           pos          (uix/state {:x 150 :y 150})
           visible      (uix/state false)
           cont         (uix/ref)
           ;; set-cont (uix/with-effect (reset! rect (bounding-rect @cont)))
           r            0
           set-position (fn [e]
                          (let [mouse    (client-pos e)
                                relative (relative-pos @rect mouse)]
                            (reset! pos {:x (:x relative) :y (:y relative)})))
           mobile?      (mobile-device?)]
       [:div.glow-cont.cf
        {:ref          cont
         :onMouseEnter (fn [e]
                         (do (set-position e)
                             (reset! visible (not @visible))))
         :onMouseLeave (fn [] (reset! visible (not @visible)))
         :onMouseMove  set-position
         :onTap        (fn [e]
                         (do (set-position e)
                             (reset! visible (not @visible))))
         }
        [:> (.-div motion)
         (merge
          (if mobile?
            {:draggable   true
             :drag        (clj->js true)
             :dragElastic 0
             :dragConstraints
                          (clj->js
                           {:top    40
                            :left   40
                            :right  (:width @rect)
                            :bottom (:height @rect)})
             } {})
          {:class "glow yellow-gr"
           :style
                  {:opacity    (if @visible 1 0)
                   :transform  (str "translate(" (:x @pos) "px," (:y @pos) "px)")
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
           i (inc i)
           (+ i 5) (+ i 6)]
          (contains? right-edge i)
          [(- i 6) (- i 5)
           (dec i) i
           (+ i 4) (+ i 5)]
          :else
          [(- i 6) (- i 5) (- i 4)
           (dec i) i (inc i)
           (+ i 4) (+ i 5) (+ i 6)]
          )))

(defn update-hover-state [state index-inner scale-outer scale-inner]
  (merge @state (reduce (fn [acc j]
                          (if (= j index-inner)
                            (assoc acc j scale-inner)
                            (assoc acc j scale-outer)))
                        {} (grid-hover index-inner))))

(defn grid-dots []
  #?(:cljs
     (let [s        (uix/state {})
           p        (uix/state 0)
           done     (uix/state {})
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
                                {:class      "dot black-bg"
                                 :animate    #js {:scale (get @s i)}
                                 :transition (clj->js {:type "spring" :mass 0.5})
                                 }]])])))

;;;;
;; Flip on Hover Dots
;;;;
(defn flipping-dots
  ([] (flipping-dots 0.3 "ease-in-out" 0.2))
  ([duration easing delay]
   #?(:cljs
      (let [state          (uix/state {:hover false :done true})
            node           (r/useRef nil)
            {:as   vis
             :keys [isVisible visibilityRect]}
            (useVisibilitySensor node
                                 #js {:intervalCheck false
                                      :scrollCheck   true
                                      :resizeCheck   false})
            fired?         (uix/ref false)
            total-duration (* 1000 (+ (* delay 5) duration))
            index->delay   [5 4 3 4 5
                            4 2 1 2 4
                            3 1 0 1 3
                            4 2 1 2 4
                            5 4 3 4 5]
            start
                           (fn []
                             (do (reset! fired? true)
                                 (reset! state {:hover true :done false})
                                 (js/setTimeout #(reset! state {:hover false :done true})
                                                total-duration)))
            preview
                           (if (and (-> vis .-isVisible) (not @fired?))
                             (start))]
        (into
         [:> (.-div motion)
          {:class        "flipping-dots cf"
           :ref          node
           :onHoverStart start
           :onHoverEnd
                         (fn [] (do (swap! state assoc :hover false)))
           :onTapStart
                         (fn [] (do (reset! state {:hover true :done false})
                                    (js/setTimeout #(reset! state {:hover false :done true})
                                                   total-duration)))}]
         (map (fn [i]
                [:div
                 {:class ["flipper"
                          (if (and (:done @state)
                                   (not (:hover @state)))
                            "show-back"
                            "show-front")]
                  :style {:transition-timing-function easing
                          :transition-duration        (sec duration)
                          :transition-delay           (sec (* i delay))}}
                 [:div.dot.front.black-bg]
                 [:div.dot.back.yellow-bg]]))
         index->delay)))))



;;;;
;; Rain on Col Hover Dots
;;;;
(defn raining-dots
  ([] (raining-dots 0.25 0.3))
  ([delay duration]
   #?(:cljs
      (let [s            (uix/state {0 {:hover false :done true}
                                     1 {:hover false :done true}
                                     2 {:hover false :done true}
                                     3 {:hover false :done true}
                                     4 {:hover false :done true}})
            node         (r/useRef nil)
            {:as   vis
             :keys [isVisible visibilityRect]}
            (useVisibilitySensor node
                                 (clj->js
                                  {:intervalCheck false
                                   :scrollCheck   true
                                   :resizeCheck   false}))
            fired?       (uix/ref false)
            mobile?      (mobile-device?)
            col-duration (* (+ delay (* 5 duration)) 1000)
            variants     #js
                             {:init #js {}
                              :rain #js {:backgroundColor #js [black yellow black]}}
            hover-start
                         (fn [col _ _]
                           (do (swap! s assoc-in [col] {:hover true :done false})
                               (js/setTimeout #(swap! s assoc-in [col :done] true)
                                              col-duration)))
            start
                         (fn []
                           (do (reset! fired? true)
                               (mapv (fn [col]
                                       (js/setTimeout #(hover-start col nil nil)
                                                      (* col 250)))
                                     [0 1 2 3 4])))
            preview
                         (if (and (-> vis .-isVisible) (not @fired?))
                           (start))
            ]
        [:div.cf {:style {:width "100%"}
                  :ref   node}
         (for [col [0 1 2 3 4]] ^{:key col}
                                [:> (.-div motion)
                                 {:class        "dot-cont"
                                  :onHoverStart (partial hover-start col)
                                  :onHoverEnd
                                                (fn [_ _] (swap! s assoc-in [col :hover] false))

                                  :onTapStart
                                                (fn [_ _]
                                                  (do (swap! s assoc-in [col] {:hover true :done false})
                                                      (js/setTimeout #(swap! s assoc-in [col]
                                                                             {:done true :hover false})
                                                                     col-duration)))
                                  }
                                 (for [i [0 1 2 3 4]] ^{:key i}
                                                      [:> (.-div motion)
                                                       {:class      "dot black-bg"
                                                        :transition #js {:delay       (* i delay)
                                                                         :duration    duration
                                                                         :times       #js [0 0.99 1]
                                                                         :repeatDelay 1500}
                                                        :animate    (let [done?  (get-in @s [col :done])
                                                                          hover? (get-in @s [col :hover])]
                                                                      (if (and done? (not hover?))
                                                                        "init"
                                                                        "rain"
                                                                        ))
                                                        :variants   variants
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

(defn clamp
  "Constraints x to be in closed interval [min max]."
  [x min-x max-x] (clojure.core/max min-x (clojure.core/min max-x x)))

(defn viewport-ratio
  "Offset ratio within visible viewport"
  [rect]
  (let [{:keys [top]} rect
        win-height (.-innerHeight js/window)]
    (clamp (/ top win-height) 0 1)))

(defn look-x [client mouse rect]
  (let [mouse-with-offset
        (- (.-clientX mouse)
           (- (:left rect)
              (/ (.-innerWidth js/window) 2)))]
    (transform-range 0 (:width client) -100 90 mouse-with-offset)))

(defn look-y [client mouse scroll]
  (let [rect   (:eyes scroll)
        offset (:header scroll)]
    (transform-range 0 (+ (- (:top rect) offset) (:height client))
                     -40 40
                     (+ offset (.-screenY mouse)))))

(defn eyeball [variant rect xr yr]
  (let [client   (client-size)
        mouse    (<sub [:db/mouse])
        scroll   (<sub [:db/scroll])
        ;; scroll {:y 0 :top 0 :doc 0}
        ;; mouse  {:y 0 :x 0}
        xm       (look-x client mouse @rect)
        ym       (look-y client mouse scroll)
        xs       0
        ys       (transform-range (:doc @rect)
                                  (+ (:height client) (:doc @rect))
                                  -40 40 (:y scroll))
        variants {:mobile
                  {:style
                   {:transform
                                (str "translate( " xs "%," ys "%)")
                    :transition "transform 0s ease-out"}}
                  :follow
                  {:style
                                    {:transform  (str "translate( " xm "%," ym "%)")
                                     :transition "transform 0s ease-out"}
                   :onTransitionEnd (fn [] (xf/dispatch [:set-variant :roll]))}
                  :roll
                  {:style
                   {:transform  (str "translate( " xr "%," yr "%)")
                    :transition "transform 0.15s ease-out 5s"}
                   :onTransitionEnd
                   (fn []
                     (xf/dispatch [:set-variant :follow])
                     (xf/dispatch [:set-variant :roll]))}}]
    [:div.eyeball (variant variants)]))

(defn eye []
  (let [node    (uix/state false)
        rect    (uix/ref)
        xr      (rand-r -100 100)
        yr      (rand-r -40 40)
        mobile? (mobile-device?)
        ;; res     (<sub [:db/resize])
        variant (<sub [:db/variant])
        ]
    [:div {:ref #(when-not @node
                   (do (reset! node %)
                       (reset! rect (bounding-rect %))))}
     [:div.eye
      [:div {:class (if true "pupil blink" "pupil")}]]
     [eyeball (if mobile? :mobile :follow) rect xr yr]]))

(defn eye-dots [idx]
  #?(:cljs
     [:div.eyes-cont.cf
      (for [i (range 0 25)]
        ^{:key i}
        [:div.dot.black-bg
         (when (contains? (into #{} idx) i)
           [eye])])]))



;;;;
;; Floating Dots
;;;;
(defn rand-edge [min-x max-x min-y max-y n]
  (let [edges  [{:x min-x :y (rand-r min-y max-y)}
                {:x (rand-r min-y max-y) :y min-y}
                {:x max-x :y (rand-r min-y max-y)}
                {:x (rand-r min-y max-y) :y max-y}]
        orders [[0 1 2 3]
                [1 2 3 0]
                [2 3 0 1]
                [3 0 1 2]]
        start  (rand-nth edges)
        order  (rand-nth orders)
        idxs   (take n (cycle order))
        pos    (mapv (partial nth edges) idxs)]
    pos))

(defn floating-dots [n offsets]
  #?(:cljs
     [:svg {:viewBox "0 0 100 100"}
      (for [i (range 1 (inc n))]
        (let [w      100
              offset (rand-nth offsets)
              r      5
              size   4.5
              x-min  (- (+ 0 size) offset)
              x-max  (- (+ w offset) size)
              y-min  (- (+ 0 size) offset)
              y-max  (- (+ w offset) size)
              x      (rand-r x-min x-max)
              y      (rand-r y-min y-max)
              xs     (repeatedly 100 (fn [] {:value    (str (rand-r x-min x-max) "%")
                                             :duration (rand-r 1000 2000)}))
              ys     (repeatedly 100 (fn [] {:value    (str (rand-r y-min y-max) "%")
                                             :duration (rand-r 1000 2000)}))]
          ^{:key i}
          [:> anime
           {:cx        (clj->js xs)
            :cy        (clj->js ys)
            :loop      true
            :direction "alternate"
            :easing    "easeInOutSine"}
           [:circle {:cx x :cy y :r (str r "%") :fill "#ffcc08"}]]
          )
        )]))

;;;;
;; Gravity
;;;;
(defn make-dot [position velocity]
  {:position    position
   :velocity    velocity
   :mass        1
   :radius      5
   :restitution -0.7})

(defn frontal-area [dot]
  (/ (* Math/PI (:radius dot) (:radius dot)) 10000))

(defn drag-force [dot d]
  (let [Cd  0.47
        rho 1.22
        A   (frontal-area dot)
        Vd  (-> dot :velocity d)]
    (if (zero? Vd) 0
                   (/ (* (- 0 0.5) Cd A rho (* 3 Vd)) (Math/abs Vd)))))

(defn accel
  ([dot d] (accel dot d 0))
  ([dot d ag]
   (+ ag (/ (drag-force dot d) (:mass dot)))))

(defn position [o]
  (let [frame-rate (/ 1.0 120)
        ag         9.81
        ax         (accel o :x 0)
        ay         (accel o :y 9.81)
        vx         (+ (-> o :velocity :x) (* ax frame-rate 1))
        vy         (+ (-> o :velocity :y) (* ay frame-rate 1))
        px         (+ (-> o :position :x) (* vx frame-rate 100))
        py         (+ (-> o :position :y) (* vy frame-rate 100))]
    (make-dot {:x px :y py} {:x vx :y vy})))

(defn collision? [d1 d2]
  (let [dx    (- (-> d2 :position :x) (-> d1 :position :x))
        dy    (- (-> d2 :position :y) (-> d2 :position :y))
        r-sum (+ (-> d1 :radius) (-> d2 :radius))]
    (<= (+ (* dx dx) (* dy dy))
        (* r-sum r-sum))))

(defn acv [d1 d2 d]
  (let [d1-m (-> d1 :mass)
        d2-m (-> d2 :mass)
        d1-v (-> d1 :velocity d)
        d2-v (-> d2 :velocity d)]
    (/ (+ (* d1-v (- d1-m d2-m)) (* 2 d2-m d2-v))
       (+ d1-m d2-m))))

(defn handle-dot-dot-collision [d1 d2]
  (let [px     (-> d1 :position :x)
        py     (-> d1 :position :y)
        vx     (-> d1 :velocity :x)
        vy     (-> d1 :velocity :y)
        vx-new (acv d1 d2 :x)
        vy-new (acv d1 d2 :y)]
    (make-dot {:x (+ px (* vx-new 1)) :y (+ py (* 1 vy-new))}
              {:x vx-new :y vy-new})))

(defn dot-dot
  ([dots] (dot-dot dots []))
  ([dots xs]
   (cond (empty? dots) xs
         :else
         (dot-dot (rest dots)
                  (conj xs
                        (reduce
                         (fn [acc dot]
                           (if (collision? (first dots) dot)
                             (handle-dot-dot-collision
                              dot (first dots))
                             acc))
                         {}
                         dots))))))

(defn dot-wall [width height dot]
  (let [px          (-> dot :position :x)
        py          (-> dot :position :y)
        vx          (-> dot :velocity :x)
        vy          (-> dot :velocity :y)
        r           (:radius dot)
        restitution (:restitution dot)]
    (cond (> py (- height r))
          (dot-wall width height (make-dot {:x px :y (- height r)}
                                           {:x vx :y (* vy restitution)}))
          (< py 0)
          (dot-wall width height (make-dot {:x px :y r}
                                           {:x vx :y (* vy restitution)}))
          (> px (- width r))
          (dot-wall width height (make-dot {:x (- width r) :y py}
                                           {:x (* vx restitution) :y vy}))
          (< px r)
          (dot-wall width height (make-dot {:x r :y py}
                                           {:x (* vx restitution) :y vy}))
          :else dot)))

(def move (comp position (partial dot-wall 100 100)))

(defn use-raf [cb]
  (uix/effect! (fn []
                 (js/window.requestAnimationFrame cb)
                 [cb])))

(defn delta-y [prev next]
  (let [y0 (-> prev :velocity :y)
        y1 (-> next :velocity :y)]
    (Math/abs (- y1 y0))))

(defn rest? [prev next]
  (< (delta-y prev next) 0.00001))

(defn all-in-rest? [prev next]
  (every? true? (map rest? prev next)))

(defn rand-dots [x-min x-max y-min y-max]
  (repeatedly 7 #(make-dot {:x (rand-r x-min x-max) :y y-min}
                           {:x 0 :y (rand-r 1 10)})))

(defn gravity-dots [n]
  #?(:cljs
     (let [node      (uix/ref)
           rect      (uix/ref)
           w         100
           offset    0
           r         4.8
           size      4.6
           x-min     (- (+ 0 size) offset)
           x-max     (- (+ w offset) size)
           y-min     (- (+ 0 (* 2 size)) offset)
           y-max     (- (+ w offset) size)
           init-dots (rand-dots x-min x-max y-min y-max)
           dots      (uix/state init-dots)
           raf       (use-raf (fn [] (let [prev @dots
                                           next (mapv move prev)]
                                       (if (rest? (first prev) (first next))
                                         (do (reset! dots init-dots))
                                         (reset! dots next)))))]
       (into [:svg {:viewBox "0 0 100 100"}]
             (map (fn [dot]
                    [:circle
                     {:cx   (-> dot :position :x)
                      :cy   (-> dot :position :y)
                      :r    r
                      :fill "#ffcc08"}]))
             @dots))))

;;;;
;; Emoji Game
;;;;
(def emojies
  {0  {:unicode "U+1F648" :icon "🙈" :name "see-no-evil monkey"}
   1  {:unicode "U+1F620" :icon "😠" :name "angry face"}
   2  {:unicode "U+1F62D" :icon "😭" :name "loudly crying face"}
   3  {:unicode "U+1F643" :icon "🙃" :name "upside-down face"}
   4  {:unicode "U+1F634" :icon "😴" :name "sleeping face"}
   5  {:unicode "U+1F61C" :icon "😜" :name "winking face with tongue"}
   6  {:unicode "U+1F633" :icon "😳" :name "flushed face"}
   7  {:unicode "U+1F611" :icon "😑" :name "expressionless face"}
   8  {:unicode "U+1F60E" :icon "😎" :name "smiling face with sunglasses"}
   9  {:unicode "U+1F62C" :icon "😬" :name "grimacing face"}
   10 {:unicode "U+1F60D" :icon "😍" :name "smiling face with heart-eyes"}
   11 {:unicode "U+1F60B" :icon "😋" :name "face savoring food"}
   12 {:unicode "U+1F605" :icon "😅" :name "grinning face with sweat"}})

(defn emoji [icon] [:span.emoji {:role "img"} icon])

(defn number->emoji [n]
  (emoji (:icon (get emojies n))))

(defn make-token [{:keys [value index]}]
  {:value value :index index :reveal false :pick false})

(defn eq-token? [t1 t2]
  (and (= (:value t1) (:value t2))
       (= (:index t1) (:index t2))))

(defn eqv-token? [t1 t2]
  (and (= (:value t1) (:value t2))
       (not (= (:index t1) (:index t2)))))

(defn game-init-state []
  {0  (make-token {:value 2 :index 0})
   1  (make-token {:value 4 :index 1})
   2  (make-token {:value 6 :index 2})
   3  (make-token {:value 8 :index 3})
   4  (make-token {:value 10 :index 4})

   5  (make-token {:value 1 :index 5})
   6  (make-token {:value 3 :index 6})
   7  (make-token {:value 5 :index 7})
   8  (make-token {:value 7 :index 8})
   9  (make-token {:value 9 :index 9})

   10 (make-token {:value 11 :index 10})
   11 (make-token {:value 12 :index 11})
   12 (make-token {:value 0 :index 12})
   13 (make-token {:value 11 :index 13})
   14 (make-token {:value 12 :index 14})

   15 (make-token {:value 2 :index 15})
   16 (make-token {:value 4 :index 16})
   17 (make-token {:value 6 :index 17})
   18 (make-token {:value 8 :index 18})
   19 (make-token {:value 10 :index 19})

   20 (make-token {:value 9 :index 20})
   21 (make-token {:value 7 :index 21})
   22 (make-token {:value 5 :index 22})
   23 (make-token {:value 3 :index 23})
   24 (make-token {:value 1 :index 24})})

(defn shuffle-tokens []
  (map-indexed (fn [i v] (make-token {:value v :index i}))
               (flatten [(shuffle (range 1 13)) 0 (shuffle (range 1 13))])))

(defn game-rand-init-state []
  (reduce (fn [acc t] (assoc acc (:index t) t)) {} (shuffle-tokens)))

(defn game-end? [state]
  (every? true? (map :reveal (vals state))))

(defn token [i state picks]
  (let [self  (get @state i)
        value (:value self)]
    [:> (.-div motion)
     {:class      (str "flipper "
                       (if (or (:reveal self)
                               (:pick self)) "show-back" "show-front"))
      :style      {:transition-timing-function "ease-in"
                   :transition-duration        (sec 0.15)}
      :on-click   (fn []
                    (let [reveal (eqv-token? self @picks)]
                      (swap! state assoc-in [(:index @picks) :pick] false)
                      (if reveal
                        (do
                          (swap! state assoc-in [(:index @picks) :reveal] true)
                          (swap! state assoc-in [i :reveal] true))
                        (do
                          (swap! state assoc-in [i :reveal] false)))
                      (swap! state assoc-in [i :pick] true)
                      (reset! picks self)
                      ))
      :onHoverEnd (fn []
                    (swap! state assoc-in [i :pick] false))
      }
     ;; [:div.dot.front.yellow-bg [:div.index (str (:value self))]]
     [:div.dot.front.yellow-bg (number->emoji (:value self))]
     [:div.dot.back.black-bg]]))

(defn on-game-end [state]
  (let [delay 75]
    (do (mapv (fn [i]
                (js/setTimeout
                 #(swap! state assoc-in [i :reveal] false)
                 (* delay i)))
              (range 0 25))
        (js/setTimeout
         #(do (reset! state (game-rand-init-state))
              (swap! state assoc-in [12 :reveal] true))
         (* delay 25)))))

(defn emoji-game []
  #?(:cljs
     (let [
           state    (uix/state (game-init-state))
           node     (r/useRef nil)
           picks    (uix/ref {:index 12 :value 0})
           revealed (uix/ref #{0})
           p        (when (game-end? @state)
                      (on-game-end state))
           {:as   vis
            :keys [isVisible visibilityRect]}
           (useVisibilitySensor node
                                (clj->js
                                 {:intervalCheck false
                                  :scrollCheck   true
                                  :resizeCheck   false}))
           fired?   (uix/ref false)
           start
                    (fn []
                      (do (reset! fired? true)
                          (swap! state assoc-in [12 :reveal] true)))
           preview
                    (if (and (-> vis .-isVisible) (not @fired?))
                      (start))
           ]
       [:> (.-div motion)
        {:class        "flipping-dots emoji-game cf"
         :ref          node
         :onHoverStart (fn [] (swap! state assoc-in [12 :reveal] true))
         }
        (for [i (range 0 25)]
          ^{:key i}
           [token i state picks]
          )]
       )))

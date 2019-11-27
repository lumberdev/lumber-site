(ns lumber.dots
  (:require
   [uix.dom.alpha :as uix.dom]
   [uix.core.alpha :as uix]
   #?(:cljs ["react-anime" :default anime])
   #?(:cljs ["framer-motion" :refer (motion useMotionValue)])
   ))

(def black "#000")
(def yellow "#FFCC08")
(defn sec [n] (str n "s"))
(defn per [n] (str n "%"))

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
                 (fn [] (do
                          ;; (swap! done assoc i false)
                          (reset! s (update-hover-state s i 0.65 0.3))
                          ;; (js/setTimeout #(swap! done assoc i true) (* 1000 duration))
                          ))
                 :onHoverEnd
                 (fn [] (do
                          (reset! s (update-hover-state s i 1 1))))
                 :onTapStart
                 (fn [] (do (reset! s (update-hover-state s @p 1 1))
                            (reset! s (update-hover-state s i 0.65 0.3))
                            (reset! p i)))
                 }
                [:> (.-div motion)
                 {:class "dot black-bg"
                  :style {:width "100%" :padding-bottom "100%"}
                  :animate #js {:scale (get @s i)}
                  :transition #js {:ease "easeOut" :duration duration}
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
                (fn [] (do
                         (prn "tap")
                         (reset! state {:hover true :done false})
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
                      :style {:width "100%" :padding-bottom "100%"}
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
;; Glow Mouse on Hover Dots
;;;;
(defn glow-dots []
  #?(:cljs (let [s (uix/state {})]
             [:div.cf {:style {:width "100%"}}
              (for [i (range 0 25)] ^{:key i}
                [:> (.-div motion)
                 {:class "dot-cont"
                  :onHoverStart
                  (fn [_ _] (reset! s 0))
                  :onHoverEnd
                  (fn [_ _] (reset! s 1))
                  :whileHover #(reset! s 2)
                  }
                 [:> (.-div motion)
                  {:class "dot black-bg"
                   :style {:width "100%" :padding-bottom "100%"}
                   :animate #js {:scale (get @s i)}
                   :transition #js {:ease "linear" :duration 0.1}
                   }]])])))



;;;;
;; Follow Mouse on Hover Dots
;;;;
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

(defn transform-range [s0-min s0-max s1-min s1-max s0-value]
  (let [s0 (- s0-max s0-min)
        s1 (- s1-max s0-min)]
    (+ s1-min (/ (* s1 (- s0-value s0-min)) s0))))

(defn eye-dots [pos]
  #?(:cljs (let [rect (uix/ref {})
                 client (uix/ref {})
                 relative (uix/state {})
                 xm (uix/ref 0)
                 ym (uix/ref 0)
                 xe (uix/state 60)
                 ye (uix/state 38)]
             [:> (.-div motion)
              {:class "cf"
               :onHoverStart (fn [e] (reset! rect (bounding-rect e)))
               :onHoverEnd (fn [e] (reset! relative {:x (/ (:width @rect) 2)
                                                     :y (/ (:width @rect) 2)}))
               :onMouseMove (fn [e] (reset! relative (relative-pos @rect (client-pos e))))
               :style {:width "100%"}}
              (for [i (range 0 25)] ^{:key i}
                (do [:> (.-div motion)
                     {:animate #js {}
                      :class "dot black-bg"
                      :style {:float "left" :width "20%" :padding-bottom "20%" :pointer-events "none"}}
                     (when (contains? (into #{} pos) i)
                       [:div
                        [:> (.-div motion)
                         {:class "eye"}
                         [:> (.-div motion)
                          {:class "pupil"
                           :animate #js {:height #js ["0%" "100%" "0%"]}
                           :transition #js {:duration 0.5 :repeatDelay 5 :loop 100}
                           }]
                         ]
                        [:> (.-div motion)
                         {:animate #js {}
                          :class "eyeball"
                          :style {:top
                                  (str (transform-range 0 (:height @rect) 25 22 (:y @relative)) "%")
                                  :left
                                  (str (transform-range 0 (:width @rect) 12 45 (:x @relative)) "%")}
                          }]])
                     ]))])))



;;;;
;; Floating Dots
;;;;
(defn rand-r [min max]
  (+ (* (rand) (- max min)) min))

(defn floating-dots [n offsets]
  #?(:cljs [:svg
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

(defn bumping-dots [n]
  #?(:cljs [:svg
            (for [i (range 1 (inc n))]
              (let [w 100
                    offset 0
                    r 5
                    size 4.5
                    x-min (- (+ 0 size) offset)
                    x-max (- (+ w offset) size)
                    y-min (- (+ 0 size) offset)
                    y-max (- (+ w offset) size)
                    x (rand-r x-min x-max)
                    y (rand-r y-min y-max)
                    re (rand-edge x-min x-max y-min y-max 100)
                    durations (repeatedly 100 #(rand-r 1000 1500))
                    xs (mapv (fn [p d] {:value (str (:x p) "%")
                                        :duration d}) re durations)
                    ys (mapv (fn [p d] {:value (str (:y p) "%")
                                        :duration d}) re durations)
                    ]
                [:> anime
                 {:cx (clj->js xs)
                  :cy (clj->js ys)
                  :loop true
                  :direction "alternate"
                  :easing "linear"}
                 [:circle {:cx x :cy y :r (str r "%") :fill "#ffcc08"}]]
                )
              )]))

(defn spring-dots [n]
  #?(:cljs [:svg
            (for [i (range 1 (inc n))]
              (let [w 100
                    offset 0
                    r 5
                    size 4.5
                    x-min (- (+ 0 size) offset)
                    x-max (- (+ w offset) size)
                    y-min (- (+ 0 size) offset)
                    y-max (- (+ w offset) size)
                    x (rand-r x-min x-max)
                    y (rand-r y-min y-max)
                    re (rand-edge x-min x-max y-min y-max 100)
                    durations (repeatedly 100 #(rand-r 1000 1500))
                    xs (mapv (fn [p d] {:value (str (:x p) "%")
                                        :duration d}) re durations)
                    ys (mapv (fn [p d] {:value (str (:y p) "%")
                                        :duration d}) re durations)
                    ]
                [:> (.-circle motion)
                 {:cx x :cy y :r (str r "%") :fill "#ffcc08"
                  :animate #js {
                                ;; :cx (clj->js (mapv :value xs))
                                ;; :cy (clj->js (mapv :value ys))
                                :cx (clj->js [5 20 95])
                                :cy (clj->js [5 120 40])
                                }
                  :transition #js {:type "spring"}
                  }
                 ]
                )
              )]))

(defn pos [x y] {:x x :y y})

(defn rand-pos [w h]
  (pos (rand-int w) (rand-int h)))

(defn rand-pos-seq [n w h]
  (repeatedly n #(rand-pos w h)))

(defn accumulate [xs]
  (reduce
   (fn [acc n] (conj acc (reduce + 0 (take n xs))))
   []
   (range 1 (inc (count xs)))))

(defn random-force-seq [min max n]
  (last (take 3 (iterate accumulate (repeatedly n #(rand-r min max))))))

(defn filter< [n xs] (filter #(> n %) xs))
(defn filter> [n xs] (filter #(< n %) xs))
(defn filter-range [min max xs] (filter< max (filter> min xs)))

(defn rand-force [min max fmin fmax n]
  (take n
        (filter-range min max
                      (map (fn [x] (/ x (* 2 n)))
                           (random-force-seq fmin fmax (* 2 n))))))

(ns lumber.dots
  (:require
   [uix.dom.alpha :as uix.dom]
   [uix.core.alpha :as uix]
   ["react-anime" :default anime]
   ["framer-motion" :refer (motion useMotionValue)]
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

(defn update-hover-state [state i scale]
  (merge @state (reduce (fn [acc i] (assoc acc i scale)) {} (grid-hover i))))

(defn grid-dots []
  (let [s (uix/state {})]
    [:div.cf {:style {:width "100%"}}
     (for [i (range 0 25)]
       [:> (.-div motion)
        {:class "dot-cont"
         :onHoverStart
           (fn [_ _] (reset! s (update-hover-state s i 0.65)))
         :onHoverEnd
           (fn [_ _] (reset! s (update-hover-state s i 1)))
         :whileHover
           #(swap! s assoc i 0.3)}
        [:> (.-div motion)
         {:class "dot black-bg"
          :style {:width "100%" :height "100%" :padding-bottom "100%"}
          :animate #js {:scale (get @s i)}
          :transition #js {:ease "linear" :duration 0.1}
          }]])]))



;;;;
;; Flip on Hover Dots
;;;;
(defn flipping-dots
  ([] (flipping-dots 0.3 "ease-in-out" 0.2))
  ([duration easing delay]
   (let [state          (uix/state {:hover false :done true})
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
         (fn [] (do (swap! state assoc :hover false)))}
      (for [i index->delay]
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
        )])))



;;;;
;; Rain on Col Hover Dots
;;;;
(defn raining-dots
  ([] (raining-dots 0.25 0.3))
  ([delay duration]
   (let [s (uix/state {0 {:hover false :done true}
                       1 {:hover false :done true}
                       2 {:hover false :done true}
                       3 {:hover false :done true}
                       4 {:hover false :done true}})
         col-duration  (* (+ delay (* 5 duration)) 1000)
         variants #js
         {:init #js {}
          :rain #js {:background-color #js [black yellow black]}}]
     [:div.cf {:style {:width "100%"}}
      (for [col [0 1 2 3 4]]
        [:> (.-div motion)
         {:class "dot-cont"
          :onHoverStart
          (fn [_ _] (do (swap! s assoc-in [col] {:hover true :done false})
                        (js/setTimeout #(swap! s assoc-in [col :done] true) col-duration)))
          :onHoverEnd
          (fn [_ _] (swap! s assoc-in [col :hover] false))}
         (for [i [0 1 2 3 4]]
           [:> (.-div motion)
            {:class "dot black-bg"
             :style {:width "100%" :height "100%" :padding-bottom "100%"}
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
        )])))



;;;;
;; Glow Mouse on Hover Dots
;;;;
(defn glow-dots []
  (let [s (uix/state {})]
    [:div.cf {:style {:width "100%"}}
     (for [i (range 0 25)]
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
          :style {:width "100%" :height "100%" :padding-bottom "100%"}
          :animate #js {:scale (get @s i)}
          :transition #js {:ease "linear" :duration 0.1}
          }]])]))



;;;;
;; Follow Mouse on Hover Dots
;;;;
(defn bounding-rect [e]
  (let [r (-> e .-target .getBoundingClientRect)]
    {:left (.-left r) :top (.-top r)}))

(defn client-pos [e]
  {:x (-> e .-clientX) :y (-> e .-clientY)})

(defn screen-pos [e]
  {:x (-> e .-screenX) :y (-> e .-screenY)})

(defn relative-pos [rect global]
  {:x (- (:x global) (:left rect))
   :y (- (:y global) (:top rect))})

(defn eye-dots [pos]
  (let [rect (uix/ref {})
        client (uix/ref {})
        relative (uix/ref {})
        xm (uix/ref 0)
        ym (uix/ref 0)
        xe (uix/state 60)
        ye (uix/state 35)]
    [:> (.-div motion)
     {:class "cf"
      :onHoverStart (fn [e]
                      (reset! rect (bounding-rect e))
                      (prn "rect:" @rect))
      :onMouseOver (fn [e]
                     (do (reset! client (client-pos e))
                         (reset! relative (relative-pos @rect @client))))
      :style {:width "100%"}}
     (for [i (range 0 25)]
       (do [:> (.-div motion)
            {:animate #js {}
             :class "dot" :style {:float "left"
                     :width "20%"
                     :padding-bottom "20%"}}
            (when (contains? pos i)
              [:div
               [:> (.-div motion)
                    {:animate #js {}
                     :class "eye"}]
               [:> (.-div motion)
                {:animate #js {}
                 :class "eyeball"
                 :style {:top "36%" :left (str @ye "%")}
                 }]])
            ]))]))



;;;;
;; Floating Dots
;;;;
(defn rand-r [min max]
  (+ (* (rand) (- max min)) min))

(defn floating-dots [n offsets]
  [:svg
   (for [i (range 1 (inc n))]
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
        [:circle {:cx x :cy y :r (str r "%") :fill "#ffcc08"}]])
     )])

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

;; (defn bumping-dots [n]
;;   [:svg
;;    (for [i (range 1 (inc n))]
;;      (let [w 100
;;            offset 0
;;            r 5
;;            size 4.5
;;            x-min (- (+ 0 size) offset)
;;            x-max (- (+ w offset) size)
;;            y-min (- (+ 0 size) offset)
;;            y-max (- (+ w offset) size)
;;            x (rand-r x-min x-max)
;;            y (rand-r y-min y-max)
;;            re (rand-edge x-min x-max y-min y-max 100)
;;            durations (repeatedly 100 #(rand-r 1000 1500))
;;            xs (mapv (fn [p d] {:value (str (:x p) "%")
;;                                :duration d}) re durations)
;;            ys (mapv (fn [p d] {:value (str (:y p) "%")
;;                                :duration d}) re durations)
;;            ]
;;        [:> anime
;;         {:cx (clj->js xs)
;;          :cy (clj->js ys)
;;          :loop true
;;          :direction "alternate"
;;          :easing "linear"}
;;         [:circle {:cx x :cy y :r (str r "%") :fill "#ffcc08"}]]
;;        )
;;      )])



(def w 100)
(def size 4.5)
(def offset 0)
(mapv #(str (:x %) "%") (rand-edge (- (+ 0 size) offset)
                                   (- (+ w offset) size)
                                   (- (+ 0 size) offset)
                                   (- (+ w offset) size)
                                   30))

(defn bumping-dots [n]
  ;; [:div.cf {:style {:width "100%" :position "absolute" :top 0 :right 0 :bottom 0 :left 0}}
  [:svg
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
           ;; re (rand-edge x-min x-max y-min y-max 100)
           re (rand-edge 4.8 95.2 4.8 95.2 100)
           durations (repeatedly 100 #(rand-r 1000 1500))
           xs (mapv (fn [p] (str (:x p) "%")) re)
           ys (mapv (fn [p] (str (:y p) "%")) re)
           ]
       [:> (.-circle motion)
        {:cx x
         :cy y
         :r (str r "%")
         :fill "#ffcc08"
         :style {:z-index 1}
         :animate #js {
                       :cx (clj->js xs)
                       :cy (clj->js ys)

                       ;; :cx (per 50)
                       ;; :cy (per 50)
                       }

         :transition #js {

                          ;; :type "spring"
                          ;; :damping 1
                          ;; :mass 50

                          :type "inertia"
                          ;; :modifyTarget (fn [target] target)
                          :bounceStiffness 100  ;; 500
                          :bounceDamping 8      ;; 10
                          :power 0.8            ;; 0.8
                          ;; :timeConstant 700     ;; 700
                          ;; :restDelta 0.01       ;; 0.01
                          :min (per 4.7)
                          :max (per 95.3)
                          ;; :duration (clj->js durations)
                          :duration 200

                          }
         }
        ]
       ;; [:> (.-div motion)
       ;;  {:class "yellow-bg"
       ;;   :style {:position "absolute"
       ;;           :left x
       ;;           :top y
       ;;           :width (per (* 2 r))
       ;;           :height (per (* 2 r))
       ;;           :padding-top (per (* 2 r))
       ;;           }
       ;;   :animate #js {
       ;;                 :top (clj->js xs) ;;(per x-min) ;;(clj->js xs)
       ;;                 :left (clj->js ys);;(per y-min) ;;(clj->js ys)
       ;;                 }
       ;;   :transition #js {:type "spring"
       ;;                    :damping 1
       ;;                    :mass 50
       ;;                    :min (per 20)
       ;;                    :max (per 80)
       ;;                    :bounceStiffness 100000}
       ;;   }
       ;;  ]
       )
     )])

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

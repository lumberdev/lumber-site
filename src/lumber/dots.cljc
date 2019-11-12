(ns lumber.dots
  (:require
   [uix.dom.alpha :as uix.dom]
   [uix.core.alpha :as uix]
   ["react-anime" :default anime]
   ["framer-motion" :refer (motion useMotionValue)]
   ))

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
         :whileHover #(swap! s assoc i 0.3)}
        [:> (.-div motion)
         {:class "dot"
          :style {:width "100%" :height "100%" :padding-bottom "100%"}
          :animate #js {:scale (get @s i)}
          :transition #js {:ease "linear" :duration 0.1}
          }]]
       )]))

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

;; (def eds [{:x min-x :y (rand-r min-y max-y)}
;;   {:x (rand-r min-y max-y) :y min-y}
;;   {:x max-x :y (rand-r min-y max-y)}
;;   {:x (rand-r min-y max-y) :y max-y}])
;; (def idxs (take 8 (cycle (rand-nth [[0 1 2 3]
;;                            [1 2 3 0]
;;                            [2 3 0 1]
;;                            [3 0 1 2]]))))

(defn bumping-dots [n]
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
           re (rand-edge x-min x-max y-min y-max 100)
           durations (repeatedly 100 #(rand-r 1000 1500))
           xs (mapv (fn [p d] {:value (str (:x p) "%")
                               :duration d}) re durations)
           ys (mapv (fn [p d] {:value (str (:y p) "%")
                               :duration d}) re durations)
           ]
       ;; [:> (.-circle motion)
       ;;  {:cx x
       ;;   :cy y
       ;;   :r (str r "%")
       ;;   :fill "#ffcc08"
       ;;   :animate #js {:cx [0.0 50.0 100.0]}
       ;;   :transition #js {:duration 1 :times [0 0.2 1]}
       ;;   }]
       [:> anime
        {:cx (clj->js xs)
         :cy (clj->js ys)
         :loop true
         :direction "alternate"
         :easing "linear"}
        [:circle {:cx x :cy y :r (str r "%") :fill "#ffcc08"}]]
       )
     )])

(defn pos [x y] {:x x :y y})

(defn rand-pos [w h]
  (pos (rand-int w) (rand-int h)))

(defn rand-pos-seq [n w h]
  (repeatedly n #(rand-pos w h)))

;; (deftest test-accumulate
;;   (is (= [1 (+ 1 2) (+ 1 2 3) (+ 1 2 3 4)] (accumulate [1 2 3 4])))
;;   (is (= [1 (+ 1 2)] (accumulate [1 2])))
;;   (is (= [1] (accumulate [1])))
;;   (is (= [] (accumulate []))))
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

(defn rand-step [s x]
  (let [p (rand-int 10)]
    (if (> p 5) (+ x s) (- x s))))

(defn rand-s-seq
  [seed step n]
  (take n (iterate (partial rand-step step) seed)))

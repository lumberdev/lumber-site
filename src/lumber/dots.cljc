(ns lumber.dots)

(defn pos [x y] {:x x :y y})

(defn rand-pos [w h]
  (pos (rand-int w) (rand-int h)))

(defn rand-pos-seq [n w h]
  (repeatedly n #(rand-pos w h)))

;; (defn matrix-pos-seq [n w h]
;;   (take n (iterate ())))

;; (defn matrix-pos [n i prev]
;;   (cond (zero? n) ))

(def dot-matrix
  [[{:x 40 :y 40}  {:x 120 :y 40}  {:x 200 :y 40}]
   [{:x 40 :y 120} {:x 120 :y 120} {:x 200 :y 120}]
   [{:x 40 :y 200} {:x 120 :y 200} {:x 200 :y 200}]])

(def dirs [[1 1] [1 -1] [-1 1] [-1 -1]])
(def w 320)
(def h 300)
(def x (rand-int w))
(def y (rand-int h))

(defn edge? [p]
  (let [x (:x p)
        y (:y p)]
    (cond (<= x 30) true
          (<= y 30) true
          (>= x 290) true
          (>= y 270) true
          :else false)))

(defn next-position [d p]
  {:x (+ (:x p) (first d)) :y (+ (:y p) (second d)) :d d})

(def d (rand-nth dirs))

(defn move [d p]
  (let [np (next-position d p)]
    (cond (edge? np) (move (rand-nth dirs) p)
          :else np)))

;; (take 100 (iterate (partial move d) {:x 250 :y 240}))

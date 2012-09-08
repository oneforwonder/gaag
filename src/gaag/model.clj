(ns gaag.model)
    ;(:use [clojure.math.numeric-tower :only [abs]]))

;Definitions of game material

(defrecord Piece [animal side frozen?])

(def animals [:elephant :camel :horse :dog :cat :rabbit])

(def animal-ranks (zipmap (reverse animals) (range)))

(def animal-counts
    {:elephant 1
     :camel    1
     :horse    2
     :dog      2
     :cat      2
     :rabbit   8})

(def sides [:gold :silver])

(def columns [:a :b :c :d :e :f :g :h])
(def rows [:1 :2 :3 :4 :5 :6 :7 :8])

(def trap-squares [[:c :3] [:f :3] 
                   [:c :6] [:f :6]])

;Functions and shit
;
(defn my-abs
  "Absolute value of a number."

  [n]
  (if (< 0 n) (- n) n))

(defn cr-to-xy [[c r]]
  [(.indexOf columns c)
   (.indexOf rows r)])

(defn xy-to-cr [[x y]]
  [(get columns x)
   (get rows y)])

(defn square-occupied? [square board]
  (contains? board square))

(defn square-empty? [square board]
  (not (contains? board square)))

(defn squares-adjacent? [sq1 sq2] 
  (let [[x1 y1] (cr-to-xy sq1)
        [x2 y2] (cr-to-xy sq2)
        dx (my-abs (- x1 x2))
        dy (my-abs (- y1 y2))]
        (= [0 1] (sort [dx dy]))))

(defn adjacent-squares [square]
  (let [[x y] (cr-to-xy square)]
    (keep (fn [[dx dy]] 
            (let [ax (+ x dx)
                  ay (+ y dy)]
              (when (and (<= 0 ax 7)
                         (<= 0 ay 7))
                (xy-to-cr [ax ay]))))
          [[0 1] [0 -1] [1 0] [-1 0]])))

(defn adjacent-pieces [square board]
  (keep board (adjacent-squares square)))

(defn adjacent-friends [square board]
  (if-let [p (get board square)] 
    (filter #(= (:side p) (:side %)) (adjacent-pieces square board))))

(defn adjacent-enemies [square board]
  (if-let [p (get board square)] 
    (filter #(not= (:side p) (:side %)) (adjacent-pieces square board))))


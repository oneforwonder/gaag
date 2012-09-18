(ns gaag.model
    (:use [gaag.decorate :only [decorate]]
          [gaag.validation :only [returns-valid-coord returns-valid-square]]
          [clojure.pprint :only [pprint]]))

(defn piece 
  ([a s] (piece a s false)) 
  ([a s f] {:animal a :side s :frozen? f}))

(def animals [:elephant :camel :horse :dog :cat :rabbit])

(def animal-ranks (zipmap (reverse animals) (range)))

(defn rank [p]
  (animal-ranks (:animal p)))

(def animal-counts
    {:elephant 1, :camel 1, :horse 2, :dog 2, :cat 2, :rabbit 8})

(def sides [:gold :silver])

(def columns [:a :b :c :d :e :f :g :h])
(def rows [:1 :2 :3 :4 :5 :6 :7 :8])

(def home-rows
    {:gold   [:1 :2]
     :silver [:8 :7]})

(def trap-squares [[:c :3] [:f :3] 
                   [:c :6] [:f :6]])

(def all-squares (for [c columns r rows] [c r]))

(defn abs [n] 
  (if (< 0 n) (- n) n))

(defn pret [s]
  (println s)
  s)

(defn cr-to-xy [[c r]]
  [(.indexOf columns c)
   (.indexOf rows r)])
(decorate cr-to-xy returns-valid-coord)

(defn xy-to-cr [[x y]]
  [(nth columns x)
   (nth rows y)])
(decorate xy-to-cr returns-valid-square)

(defn square-occupied? [square board]
  (not (nil? (get board square))))

(defn square-empty? [square board]
  (nil? (get board square)))

(defn squares-adjacent? [sq1 sq2] 
  (let [[x1 y1] (cr-to-xy sq1)
        [x2 y2] (cr-to-xy sq2)
        dx (abs (- x1 x2))
        dy (abs (- y1 y2))]
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
    (filter #(= (:side p) (:side %))  
            (adjacent-pieces square board)))) 

(defn adjacent-enemies [square board]
  (if-let [p (get board square)] 
    (filter #(not= (:side p) (:side %)) 
            (adjacent-pieces square board))))

(defn frozen? [square board]
  (if-let [p (get board square)]
    (let [afs (adjacent-friends square board)
          aes (adjacent-enemies square board)]
      (boolean
        (and (empty? afs)
             (not (empty? aes))
             (some (fn [e] (< (rank p) (rank e))) aes))))))


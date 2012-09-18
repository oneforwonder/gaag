(ns gaag.validation
    (:use clojure.test
          [clojure.repl :only [demunge]]
          [clojure.string :only [split]]
          [clojure.pprint :only [pprint]]))

;; Egads, duplication. This is bad.
;; ...but it's an easy way around a circular dependency.
(def animals [:elephant :camel :horse :dog :cat :rabbit])
(def sides [:gold :silver])

(def columns [:a :b :c :d :e :f :g :h])
(def rows [:1 :2 :3 :4 :5 :6 :7 :8])

(def coords #{0 1 2 3 4 5 6 7})

(comment
(defn bid [board]
  (reduce + (map (fn [[sq p]] 
                   (let [[x y] (cr-to-xy sq)]
                     (+ (* 64 x) (* 8 y) (rank p))))
                 board)))) 

(comment
(defn valid-coord? [coord]
  (boolean
    (and
      (is (= (count coord) 2))
      (is (coords (first coord)))
      (is (coords (second coord))))))) 

(defn valid-coord? [coord]
  (boolean
    (and
      (= (count coord) 2)
      (coords (first coord))
      (coords (second coord)))))

(defn valid-square? [sq]
  (boolean
    (and 
      (is (= (count sq) 2))
      (is ((set columns) (first sq)))
      (is ((set rows) (second sq))))))

(defn valid-piece? [p]
  (boolean
    (and
      (is ((set sides) (:side p)))
      (is ((set animals) (:animal p)))
      (is (contains? #{true false} (:frozen? p))))))

(defn valid-board? [b]
  (boolean
    (and
      (is (map? b)) 
      (is (<= (count b) 32)) 
      (is (every? valid-square? (keys b))) 
      (is (every? valid-piece? (vals b))) 
      (is (<= (count (filter #(= (:side %) :gold) (vals b))) 16)) 
      (is (<= (count (filter #(= (:side %) :silver) (vals b))) 16)))))


(defn fn-name [f]
  (first (split (demunge (str f)) #"@")))

(defn post-condition-decorator [post-conditions]
  (fn [f]
    (fn [& args]
        (println "Function: " (fn-name f))
        (println "Args:")
        (pprint args)
      (let [ret (apply f args)]
        (println "Ret:")
        (pprint ret)
        (if ((apply every-pred post-conditions) ret)
          ret
          (do
            (println (fn-name f) "returned an invalid result:")
            (pprint ret)
            (println \newline "Args:")
            (pprint args)
            (flush)
            (Thread/sleep 5000)
            (throw (AssertionError. (str "Post condition violated in " (fn-name f))))
            ))))))

(def returns-valid-board  (post-condition-decorator [valid-board?]))
(def returns-valid-square (post-condition-decorator [valid-square?]))
(def returns-valid-piece  (post-condition-decorator [valid-piece?]))
(def returns-valid-coord  (post-condition-decorator [valid-coord?]))

;(defn returns-valid-board [f]
  ;(fn [& args]
    ;(let [b (apply f args)]
      ;(if (valid-board? b)
        ;b
        ;(do
          ;(println (first (split (demunge (str f)) #"@")) 
                   ;"returned an invalid board state:")
          ;(pprint b)
          ;(println \newline "Args:")
          ;(pprint args)
          ;(throw (AssertionError. "Invalid board state")))))))

;(defn returns-valid-board [f]
  ;(fn [& args]
    ;(let [b (apply f args)
          ;valid-key? (fn [k] 
                         ;(assert (= (count k) 2)) 
                         ;(assert ((set columns) (first k))) 
                         ;(assert ((set rows) (second k)))
                         ;true)
          ;valid-val? (fn [v]
                         ;(assert (instance? Piece v)) 
                         ;(assert ((set sides)   (:side v))) 
                         ;(assert ((set animals) (:animal v))) 
                         ;(assert (contains? #{true false} (:frozen? v)))
                         ;true)]
      ;(try 
        ;(do
          ;(assert (map? b)) 
          ;(assert (<= (count b) 32)) 
          ;(assert (every? valid-key? (keys b))) 
          ;(assert (every? valid-val? (vals b))) 
          ;(assert (<= (count (filter #(= (:side %) :gold) (vals b))) 16)) 
          ;(assert (<= (count (filter #(= (:side %) :silver) (vals b))) 16)) 
          ;b)
        ;(catch AssertionError e 
          ;(do
            ;(println f "returned an invalid board state:")
            ;(pprint b)
            ;(throw e)))))))


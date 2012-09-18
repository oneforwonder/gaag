(ns gaag.repl-ui
    (:use [gaag.model]
          [clojure.string :only [upper-case join]] 
          [clojure.pprint :only [pprint]]))

(def abbreviate
    {:elephant :e
     :camel :m
     :horse :h
     :dog :d
     :cat :c
     :rabbit :r})

(def debreviate 
    (let [ks (keys abbreviate)
          vs (map abbreviate ks)]
      (zipmap vs ks)))

(defn piece-to-char [sp]
  (if (nil? sp) \_
    (let [[side piece] sp
          c (nth (str (abbreviate piece)) 1)]
      (if (= side :gold)
        (upper-case c)
        c))))

(defn board-to-str [board]
  (join \newline
    (for [r (reverse rows)]
      (join \space
        (for [c columns]
          (piece-to-char (get board [c r])))))))

(defn short-start-to-board [ss side]
  (let [rows (case side
               :gold   [:2 :1]
               :silver [:8 :7])]
    (reduce merge
      (for [[r row] (map vector rows ss)
            [c a]   (map vector columns row)]
           {[c r] (piece (debreviate a) side)}))))

(def silver-short-start
    [[:r :r :r :r :r :r :r :r]
     [:h :d :c :e :m :d :c :h]])

(def gold-short-start
    [[:h :d :c :m :e :r :r :r]
     [:c :d :h :r :r :r :r :r]])

(def sample-board 
     (apply merge (map (partial apply short-start-to-board) 
                       [[silver-short-start :silver]
                        [gold-short-start :gold]])))


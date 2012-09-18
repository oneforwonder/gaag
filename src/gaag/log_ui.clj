(ns gaag.log-ui
    (:use [gaag.model]
          [gaag.game]
          [gaag.repl-ui]
          [gaag.decorate :only [decorate]]
          [gaag.validation :only [returns-valid-board]]
          [clojure.java.io :only [resource]]
          [clojure.pprint :only [pprint]])
    (:require [clojure.string :as s]
              [gaag.seesaw-ui :as gui])) 

;; Parsing Arimaa game logs

(defn parse-move
  "A move string looks like 'Rd1n'. This particular move string indicates
   that the rabbit at square d1 moved north (up). During the first turn,
   when players are choosing how to position their pieces, the final
   character is omitted and only an animal and a position are provided."
  [m]
  {:animal (-> (get m 0) s/lower-case keyword debreviate)
   :square (map (comp keyword str) (.substring m 1 3))
   :action (when (< 3 (count m)) (keyword (str (get m 3))))})

(defn parse-movelist 
  "The movelist looks '...2w Eb2n Mg2n He2n\n2b ee7s ee6s md7s\n...'

   A '\n' (not an actual newline!) separates each turn. 
   A space separates each element within a turn.

   This first element in each turn contains the turn number and active side.
   The remaining elements represent moves.

   Early version of Arimaa game notation used white and black for 
   gold and silver, respectively, so both color sets are supported here."
  [ml]
  (map (fn [turn] 
           (let [[tinfo & moves] (s/split turn #" ")]
             {:number (Integer/parseInt (apply str (butlast tinfo)))
              :side   ({\w :gold 
                        \g :gold 
                        \b :silver 
                        \s :silver} (last tinfo))
              :moves  (map parse-move moves)}))
       (s/split ml #"\\n")))

(defn log-to-maps 
  "Arimaa game logs are stored in a tab-separated text tables.
   The first row contains the column headers (the keys). 
   The remaining rows contain game records (the vals)."
  [log]
  (let [lines (s/split-lines (s/replace log #" \n" ""))
        ks    (map keyword (s/split (first lines) #"\t"))]
    (map (fn [game] (zipmap ks (s/split game #"\t"))) (rest lines))))

(defn parse-games-log 
  "Parses a text log of multiple Arimaa games as found on the official website:
   http://arimaa.com/arimaa/download/gameData/

   Within the logs are moves notated in the official format:
   http://arimaa.com/arimaa/learn/notation.html"
  [log]
    (map (fn [m] {:meta  (dissoc m :movelist)
                  :turns (parse-movelist (:movelist m))})
         (log-to-maps log)))

(def games (-> (resource "games/allgames2002.txt")
               (slurp)
               (parse-games-log)))

(defn dest [src dir]
  (->> (cr-to-xy src)
       (map + ({:n [0 1]
                :e [1 0]
                :s [0 -1]
                :w [-1 0]} dir))
       (xy-to-cr)))

(defn log-move-to-game-move [mm]
  (let [src (:square mm)]
    [src (dest src (:action mm))]))


;; Playing and displaying logged games

(defn display-board! [board]
  (gui/update-board! board)
  (Thread/sleep 500)
  board) 

(defn play-setup-turn [board turn]
  (->> (:moves turn)
       (map (fn [m] {(:square m)
                     (piece (:animal m) (:side turn) false)}))
       (reduce merge board)
       (display-board!)))
(decorate play-setup-turn returns-valid-board)

(defn play-movement-turn [board turn]
  (->> (:moves turn)
       (filter #(not= :x (:action %)))
       (map log-move-to-game-move)
       (reductions apply-move board)
       (map display-board!)
       (last)))
(decorate play-movement-turn returns-valid-board)

(defn play-log-turn [board turn]
  (pprint turn)
  (if (= (:number turn) 1)
    (play-setup-turn board turn)
    (play-movement-turn board turn)))
(decorate play-log-turn returns-valid-board)

(defn play-log-game [game]
  (gui/show-gui)
  (reduce play-log-turn {} (:turns game)))

(map play-log-game (drop 1 games))


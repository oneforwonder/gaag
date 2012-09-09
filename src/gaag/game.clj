(ns gaag.game
    (:use [clojure.set] 
          [gaag.model]))

(defn valid-start-state? 
  "Determine whether this is a valid starting state according to these rules:
    - All of the squares in both home rows are full
    - No squares outside of those rows are occupied
    - The number of each type of animal is correct"
  [board] 
  (and (valid-start-state? board :gold)
       (valid-start-state? board :silver)) 
  
  [board side]
  (let [hrs (home-rows side)
        ors (difference rows hrs)
        sps (filter #(= (:side (val %)) side) board)]
    (and 
      (every? (fn [sq] (square-occupied? sq sps)) 
              (for [c columns r hrs] [c r])) 
      (every? (fn [sq] (square-empty? sq sps))
              (for [c columns r ors] [c r])) 
      (= animal-counts (frequencies (map :animal (vals sps)))))))

(defn piece-not-frozen? [square board]
  (not (:frozen? (board src))))

(defn valid-move? 
  "Determine whether this a valid move according to these rules:
    - A piece must exist at the source
    - No piece exists at the destination
    - Source and destination are adjacent
    - If the piece being moved belongs to the opponent, then either
      a) A stronger piece belonging to this player must have been
         at the destination on the previous move, or
      b) A stronger piece belonging to this player must move onto
         the source location on the next move
    - If the piece being moved belongs to the active player, then 
      the piece cannot be frozen"
  [move board active-side prev-move next-move]
  (let [[src dest] move
        p (get board src)]
      (every? identity
        [(square-occupied? src board)
         (square-empty? dest board)
         (squares-adjacent? src dest)
         (or (= (:side p) active-side)
             ; TODO
             )
         (or (not= (:side p) active-side)
             (not (:frozen? p)))])))

(defn move-piece [move board]
  (let [[src dest] move
        piece (board src)]
    (assoc (dissoc board src) dest piece)))

(defn remove-trapped [board]
  (apply dissoc board 
      (filter adjacent-friends trap-squares)))

(defn update-frozen [board]
  (reduce (fn [b [sq p]] 
              (assoc-in b [sq :frozen?] (frozen? sq b)))
          board board))

(defn apply-move [move board]
  (->> board
       (move-piece move)
       (remove-trapped)
       (update-frozen)))


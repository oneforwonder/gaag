(ns gaag.game
    (:use [gaag.model]))

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
  board ; TODO
  )

(defn apply-move [move board]
  (->> board
       (move-piece move)
       (remove-trapped)
       (update-frozen)))


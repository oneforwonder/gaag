(ns gaag.game
    (:use [gaag.model]
          [gaag.decorate :only [decorate]]
          [gaag.validation :only [returns-valid-board]]
          [clojure.set :only [difference]] 
          [clojure.pprint :only [pprint]]))

(defn valid-start-state? 
  "Determine whether this is a valid starting state according to these rules:
    - All of the squares in both home rows are full
    - No squares outside of those rows are occupied
    - The number of each type of animal is correct"
  ([board] 
   (boolean
     (and (valid-start-state? board :gold)
          (valid-start-state? board :silver)))) 
  
  ([board side] 
   (let [hrs (home-rows side)
         ors (difference (set rows) (set hrs))
         sps (filter #(= (:side (val %)) side) board)]
     (boolean
       (and 
         (every? (fn [sq] (square-occupied? sq sps)) 
                 (for [c columns r hrs] [c r])) 
         (every? (fn [sq] (square-empty? sq sps))
                 (for [c columns r ors] [c r])) 
         (= animal-counts (frequencies (map :animal (vals sps)))))))))

(defn valid-dislodge? [move board active-side prev-move next-move]
  (let [[src dest]   move
        [psrc pdest] prev-move
        [nsrc ndest] next-move
        p (get board src)]
    (boolean
      (and (not (nil? p)) 
           (not= (:side p) active-side)
           (or (if-let [pp (get board pdest)]
                       (and (= (:side pp) active-side)
                            (< (:rank p) (:rank pp)))) 
               (if-let [np (get board nsrc)]
                       (and (= (:side np) active-side)
                            (< (:rank p) (:rank np)))))))))

(defn valid-movement? [move board active-side]
  (let [[src dest] move
        p (get board src)]
    (boolean
      (and (not (nil? p))
           (= (:side p) active-side)
           (not (:frozen? p))))))

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
  (let [[src dest] move]
    (boolean
      (and
        (square-occupied? src board)
        (square-empty? dest board)
        (squares-adjacent? src dest)
        (or (valid-dislodge? move board active-side prev-move next-move)
            (valid-movement? move board active-side))))))

(defn move-piece [move board]
  (let [[src dest] move
        piece (board src)]
    (-> board
        (dissoc src)
        (assoc dest piece))))
(decorate move-piece returns-valid-board)

(defn remove-trapped [board]
  (apply dissoc board 
      (filter (fn [sq] (empty? (adjacent-friends sq board))) trap-squares)))
(decorate remove-trapped returns-valid-board)

(defn update-frozen [board]
  (reduce (fn [b [sq p]] 
              (assoc-in b [sq :frozen?] (frozen? sq b)))
          board board))
(decorate update-frozen returns-valid-board)

(defn apply-move [board move]
  (->> board
       (move-piece move)
       (remove-trapped)
       (update-frozen)))
(decorate apply-move returns-valid-board)


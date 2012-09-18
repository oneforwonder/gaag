(ns gaag.test.model
    (:use clojure.test
          gaag.model))

;; Sample board:
;;  +-----------------+
;; 8|                 |
;; 7|                 |
;; 6|     x     x     |
;; 5|   c             |
;; 4|   H e           |
;; 3|   R x     x     |
;; 2|                 |
;; 1|                 |
;;  +-----------------+
;;    a b c d e f g h 

(def p piece)

(def sample-board
  {[:b :3] (p :rabbit :gold) 
   [:b :4] (p :horse :gold) 
   [:b :5] (p :cat :silver) 
   [:c :4] (p :elephant :silver)})

(deftest test-square-conversion
  ;; From [col row] to [x y]
  (is (= (cr-to-xy [:a :1]) [0 0]))
  (is (= (cr-to-xy [:c :6]) [2 5]))
  (is (= (cr-to-xy [:h :8]) [7 7]))

  ;; From [x y] to [col row]
  (is (= (xy-to-cr [0 0]) [:a :1]))
  (is (= (xy-to-cr [2 5]) [:c :6]))
  (is (= (xy-to-cr [7 7]) [:h :8])))


(deftest test-adjacent-squares
  ;; Corner square
  (is (= (set (adjacent-squares [:a :1])) #{[:a :2] [:b :1]}))

  ;; Side square
  (is (= (set (adjacent-squares [:h :4])) #{[:h :3] [:h :5] [:g :4]}))

  ;; Center square
  (is (= (set (adjacent-squares [:b :2])) #{[:a :2] [:c :2] [:b :1] [:b :3]})))


(deftest test-adjacent-pieces
  ;; Piece on specified square, multiple adjacent pieces 
  (is (= (set (adjacent-pieces [:b :4] sample-board))
         #{(p :rabbit :gold) (p :cat :silver) (p :elephant :silver)})) 
 
  ;; No piece of specified square, multiple adjacent pieces
  (is (= (set (adjacent-pieces [:c :5] sample-board))
         #{(p :cat :silver) (p :elephant :silver)})) 

  ;; No piece on specified square, no adjacent pieces
  (is (= (set (adjacent-pieces [:f :6] sample-board)) #{})))


(deftest test-adjacent-friends
  ;; Piece on specified square, multiple adjacent pieces,
  ;; one adjacent friend
  (is (= (set (adjacent-friends [:b :4] sample-board))
         #{(p :rabbit :gold)})) 

  ;; Piece on specified square, one adjacent piece,
  ;; no adjacent friends
  (is (= (set (adjacent-friends [:c :4] sample-board)) #{})) 

  ;; No piece on specified square, multiple adjacent pieces,
  ;; no adjacent friends
  (is (= (set (adjacent-friends [:c :5] sample-board)) #{})) 

  ;; No piece on specified square, no adjacent pieces,
  ;; no adjacent friends
  (is (= (set (adjacent-friends [:f :6] sample-board)) #{})))


(deftest test-adjacent-enemies
  ;; Piece on specified square, multiple adjacent pieces,
  ;; two adjacent enemies
  (is (= (set (adjacent-enemies [:b :4] sample-board))
         #{(p :cat :silver) (p :elephant :silver)})) 

  ;; Piece on specified square, one adjacent piece,
  ;; one adjacent enemies
  (is (= (set (adjacent-enemies [:c :4] sample-board)) 
         #{(p :horse :gold)})) 

  ;; No piece on specified square, multiple adjacent pieces,
  ;; no adjacent enemies
  (is (= (set (adjacent-enemies [:c :5] sample-board)) #{})) 

  ;; No piece on specified square, no adjacent pieces,
  ;; no adjacent enemies
  (is (= (set (adjacent-enemies [:f :6] sample-board)) #{})))


(deftest test-frozen?
  ;; Only friends adjacent, not frozen
  (is (= (frozen? [:b :3] sample-board) false))

  ;; Friends and enemies of a higher rank adjacent, not frozen
  (is (= (frozen? [:b :4] sample-board) false))

  ;; Only enemies adjacent, of higher rank, frozen
  (is (= (frozen? [:b :5] sample-board) true))

  ;; Only enemies adjacent, of a lower rank, not frozen
  (is (= (frozen? [:c :4] sample-board) false)))


(ns gaag.seesaw-ui
    (:use [gaag.model :only [cr-to-xy]]
          [seesaw.core]
          [clojure.java.io :only [resource]]
          [clojure.pprint :only [pprint]]
          )
    (:import (javax.imageio ImageIO)
             (java.io File)
             (java.awt Dimension)
             javax.swing.UIManager
             ;org.pushingpixels.substance.api.SubstanceLookAndFeel
             ;org.pushingpixels.substance.api.skin BusinessSkin
             )) 

(native!)
;(javax.swing.UIManager/setLookAndFeel "org.pushingpixels.substance.api.skin.SubstanceCremeSkinLookAndFeel")
;(javax.swing.UIManager/setLookAndFeel (SubstanceBusinessLookAndFeel.))
;(SubstanceLookAndFeel/setSkin (BusinessSkin.))
;(SubstanceLookAndFeel/setSkin (CremeSkin.))

(def board-img (ImageIO/read (resource "img/board_wood.jpg")))

(defn animal-image [animal side]
  (ImageIO/read
    (resource (str "img/pieces/"
                   (get (str side) 1) 
                   "-"
                   (.substring (str animal) 1) 
                   "-t.png"))))

(def board-size [370, 370])
(def cell-pixels (* 2 41))
(def cell-offsets (map (partial * 2) [21, 20]))
(defn cell-xy-to-img-xy
  "Given cell [x,y], both in range [0,7], return the matching image pixel [x,y]." 
  [pos]
  (map + cell-offsets (map (partial * cell-pixels) pos)))

(defn draw-board! [board c g]
  (.drawImage g board-img 0 0 740 740 c)
  (doseq [[sq p] board]
    (let [a     (:animal p)
          s     (:side p)
          img   (animal-image a s)
          [x y] (cell-xy-to-img-xy (cr-to-xy sq))]
      (.drawImage g img x y 80 80 c))))

(defn canvas-fn [board]
  (fn [canvas graphics]
      (draw-board! board canvas graphics)))

(def c (canvas :paint (canvas-fn {})))
(.setMinimumSize c (Dimension. (* 2 370) (* 2 370)))

(defn update-board! [board]
  (config! c :paint (canvas-fn board))
  (.repaint c 0 0 740 740))

(def f (frame :title "Arimaa" :content c))
(.setMinimumSize f (Dimension. (+ (* 2 370) 4) (+ (* 2 370) 27)))

(defn show-gui []
  (-> f pack! show!))


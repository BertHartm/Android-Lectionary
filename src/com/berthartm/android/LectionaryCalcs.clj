(ns com.berthartm.android.LectionaryCalcs
  (:import (java.util GregorianCalendar)))

; Knuth's version of the Lilius and Clavius method of calculating Easter (post-1582)
; as found on tAOCP Volume 1 1.3.2 Question 14 (pp. 159 - 160 in my edition)
(defn GoldenNumber [year] (+ 1 (mod year 19)))
(defn Century "Calculates the century of a year" [year] (+ 1 (quot year 100)))
(defn EasterX [year] (- (quot (* 3 (Century year)) 4) 12))
(defn EasterZ [year] (- (quot (+ (* 8 (Century year)) 5) 25) 5))
(defn EasterD [year] (- (- (quot (* 5  year) 4) (EasterX year)) 10))
(defn EasterE [year] (mod (- (+ (+ (* 11 (GoldenNumber year)) 20) (EasterZ year)) (EasterX year)) 30))
(defn Epact [year] (let [E (EasterE year)]
		     (cond (= E 24) (+ E 1)
			   (and (= E 25) (> (GoldenNumber year) 11)) (+ E 1)
			   :else E)))
(defn EasterFullMoon [year] (let [E (- 44 (Epact year))] 
			      (cond (< E 21) (+ E 30)
				    :else E)))
(defn EasterSunday [year] (let [FM (EasterFullMoon year)]
			    (- (+ FM 7) (mod (+ (EasterD year) FM) 7))))
; memoize EasterSunday, not Easter, as we want Easter to return a new Calendar instance
(defn Easter [year] (let [ES (EasterSunday year)]
			    (cond (> ES 31) (new GregorianCalendar year 3 (- ES 31)) ; April
				  :else (new GregorianCalendar year 2 ES)))) ; March

(defn ChristTheKing [year]
  (let [fifthWeek (new GregorianCalendar year 10 20)]
    (let [DoW (.get fifthWeek GregorianCalendar/DAY_OF_WEEK)]
      (cond (= DoW 1) fifthWeek ; Sunday the 20th, return that
	    :else (doto fifthWeek (.add GregorianCalendar/DATE (- 8 DoW))) ; move to the next Sunday
	    ))))

(defn liturgicalYear [date]
  (let [year (.get date GregorianCalendar/YEAR)]
    ; the first day of the new year is the Wed. before ChristTheKing Sunday
    (cond (.before date (doto (ChristTheKing year) (.add GregorianCalendar/DATE -4)))
	  (nth '(:A :B :C) (mod (- year 1) 3))
	  :else ; else, we're that wednesday or after
	  (nth '(:A :B :C) (mod year 3))
	  )))
    
; to do the Christmas / Ordinary season Sunday shift, add / subtract the number of weeks, and find the next Sunday
; to go backwards (it's Sunday the X of Y), it's the ceiling of the # of weeks before, floor of the number of weeks after

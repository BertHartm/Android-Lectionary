(ns com.berthartm.android.LectionaryCalcs
  (:gen-class
   :methods [[Easter [Integer] java.util.GregorianCalendar]
	     [ChristTheKing [Integer] java.util.GregorianCalendar]
	     [liturgicalYear [java.util.GregorianCalendar] clojure.lang.Keyword]]))

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
			   true E)))
(defn EasterFullMoon [year] (let [E (- 44 (Epact year))] 
			      (cond (< E 21) (+ E 30)
				    true E)))
(defn EasterSunday [year] (let [FM (EasterFullMoon year)]
			    (- (+ FM 7) (mod (+ (EasterD year) FM) 7))))
; memoize EasterSunday, not Easter, as we want Easter to return a new Calendar instance
(defn -Easter [this year] (let [ES (EasterSunday year)]
			    (cond (> ES 31) (new java.util.GregorianCalendar year 3 (- ES 31)) ; April
				  true (new java.util.GregorianCalendar year 2 ES)))) ; March

(defn -ChristTheKing [this year] (let [fifthWeek (new java.util.GregorianCalendar year 10 20)]
				   (let [DoW (.get fifthWeek java.util.GregorianCalendar/DAY_OF_WEEK)]
				     (cond (= DoW 1) fifthWeek ; Sunday the 20th, return that
					   true (doto fifthWeek (.add java.util.GregorianCalendar/DATE (- 8 DoW))) ; move to the next Sunday
					   ))))

; TODO
(defn -liturgicalYear [this date] (nth '(:A :B :C) (mod (- (.get date java.util.GregorianCalendar/YEAR) 1) 3))) ; bad estimate for now
;(defn Between2Dates [current, start, end] (cond (and (> current start) (< current end)) true
;						true false))
;(defn LiturgicalSeason [date] )

; to do the Christmas / Ordinary season Sunday shift, add / subtract the number of weeks, and find the next Sunday
; to go backwards (it's Sunday the X of Y), it's the ceiling of the # of weeks before, floor of the number of weeks after

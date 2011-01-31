(ns com.berthartm.android.Lectionary
   (:use clj-android)
   (:require com.berthartm.android.LectionaryCalcs)
   (:import (com.berthartm.android.Lectionary R$layout R$id))
   (:import (java.util GregorianCalendar))
   )

(defn calcHolidays [year] (let [LC (new com.berthartm.android.LectionaryCalcs)]
			    (def easter (.Easter LC year))
			    (def pentecost (doto (.Easter LC year) (.add GregorianCalendar/DATE 49))) ; 50th day of Easter
			    (def ashWednesday (doto (.Easter LC year) (.add GregorianCalendar/DATE -46))) ; 40 days + 6 Sundays before Easter
			    (def transfiguration (doto (.Easter LC year) (.add GregorianCalendar/DATE -49))) ; Sunday before Ash Wednesday
			    (def formatter (new java.text.SimpleDateFormat "MMM d"))
			    (str "for " year
				 "\nEaster is " (.format formatter (.getTime easter))
				 "\nAsh Wednesday is " (.format formatter (.getTime ashWednesday))
				 "\nTransfiguration is " (.format formatter (.getTime transfiguration))
				 "\nPentecost is " (.format formatter (.getTime pentecost))
				 "\nChrist the King is " (.format formatter (.getTime (.ChristTheKing LC year)))
				 )))

; we understand only Christmas and Easter Seasons, define everything else
; Christmas is in weeks, Easter in days
; note that these definitions are unused at the moment
(def Advent '(:Christmas -4))
(def Epiphany '(:Christmas 2))
(def AshWednesday '(:Easter -46))
(def Transfiguration '(:Easter -49))
(def Lent '(:Easter -42))
(def Ascension '(:Easter 40))
(def PentecostSunday '(:Easter 49))
(def HolyTrinity '(:Easter 56))
(def Pentecost '(:Christmas -30))
(def ChristTheKing '(:Christmas -5))
(def Jan27 '(:Date 1 27))

(def bigList '(
	       ((:Date) 1 27 :A "Awesome Day")
	       ((:Date) 1 1 :* "Name of Jesus")
	       ((:Christmas -4) 1 0 :* "First Sunday of Advent")
	       ((:Christmas 0) 1 0 :* "Christmas 1")
	       ((:Christmas 0) 2 0 :* "Christmas 2")
	       ((:Easter -49) 0 :A "Transfiguration")
	       ))

(defn applicableDay [liturgicalDay date]
  (let [year (.get date GregorianCalendar/YEAR)
	litYear (.liturgicalYear (new com.berthartm.android.LectionaryCalcs) date)
	xmas (cond (< (.get date GregorianCalendar/MONTH) 3)
		   (new GregorianCalendar (- year 1) 11 25)
		   true (new GregorianCalendar year 11 25))]
    (cond (and (= (ffirst liturgicalDay) :Date)
	       (= (nth liturgicalDay 1) (+ (.get date GregorianCalendar/MONTH) 1))
	       (= (nth liturgicalDay 2) (.get date GregorianCalendar/DAY_OF_MONTH))
	       (or (= (nth liturgicalDay 3) :*)
		   (= (nth liturgicalDay 3) litYear)))
	  true
	  (and (= (ffirst liturgicalDay) :Easter)
	       (or (= (nth liturgicalDay 2) :*)
		   (= (nth liturgicalDay 2) litYear))
	       (.before (doto (.Easter (new com.berthartm.android.LectionaryCalcs) year) (.add GregorianCalendar/DATE -50))
			date) ; transfiguration (day before)
	       (.before date
			(doto (.Easter (new com.berthartm.android.LectionaryCalcs) year) (.add GregorianCalendar/DATE 50))) ; pentecost (day after)
	       (= (doto (.Easter (new com.berthartm.android.LectionaryCalcs) year) (.add GregorianCalendar/DATE (+ (last (first liturgicalDay)) (nth liturgicalDay 1)))) date)
	       )
	  true
	  (and (= (ffirst liturgicalDay) :Christmas)
	       (or (= (nth liturgicalDay 3) :*)
		   (= (nth liturgicalDay 3) litYear))
	       (or (.before date
			    (doto (.Easter (new com.berthartm.android.LectionaryCalcs) year) (.add GregorianCalendar/DATE -49))) ; transfiguration
		   (.before (doto (.Easter (new com.berthartm.android.LectionaryCalcs) year) (.add GregorianCalendar/DATE 49)) ; pentecost
			    date))
	       (= (doto xmas ; should be safe to use xmas, as it's all in one doto clause
		    (.add GregorianCalendar/DATE
			  (+ (* 7 (+ (last (first liturgicalDay)) ; Seasonal week offset
				     (second liturgicalDay))) ; this day's week offset
			     (cond (= (.get xmas GregorianCalendar/DAY_OF_WEEK) 1) -7
				   true (- 1 (.get xmas GregorianCalendar/DAY_OF_WEEK))
				   ) ; find the Sunday (Prior, so 1st sunday of X can be a '1' in the declaration)
			     (nth liturgicalDay 2)))) ; day offset (Sunday reading, Monday reading, etc.)
		  date)
	       )
	  true
	  true false))) ; base case, this day does not apply


(defactivity Main
  (:create (.setContentView context R$layout/main)
	   (let [tv (view-by-id R$id/holiday_info) hol (view-by-id R$id/holiday_list)]
	     (.setText tv (calcHolidays (.get (new GregorianCalendar) GregorianCalendar/YEAR)))
	     (on-click (view-by-id R$id/date_button)
		       (let [dp (view-by-id R$id/main_date)]
			 (.setText tv (calcHolidays (.getYear dp)))
			 (.setText hol (apply str (interpose "\n" (map last (filter (fn [possibleDay] (applicableDay possibleDay (new GregorianCalendar (.getYear dp) (.getMonth dp) (.getDayOfMonth dp)))) bigList)))))
			 )))
	   ))

(ns com.berthartm.android.Lectionary
   (:use clj-android)
   (:require [com.berthartm.android.LectionaryCalcs :as LC])
   (:use com.berthartm.android.RevisedCommonLectionary)
   (:import (com.berthartm.android.Lectionary R$layout R$id))
   (:import (java.util GregorianCalendar))
   )

(defn calcHolidays [year]
  (def easter (LC/Easter year))
  (def pentecost (doto (LC/Easter year) (.add GregorianCalendar/DATE 49))) ; 50th day of Easter
  (def ashWednesday (doto (LC/Easter year) (.add GregorianCalendar/DATE -46))) ; 40 days + 6 Sundays before Easter
  (def transfiguration (doto (LC/Easter year) (.add GregorianCalendar/DATE -49))) ; Sunday before Ash Wednesday
  (def formatter (new java.text.SimpleDateFormat "MMM d"))
  (str "for " year
       "\nEaster is " (.format formatter (.getTime easter))
       "\nAsh Wednesday is " (.format formatter (.getTime ashWednesday))
       "\nTransfiguration is " (.format formatter (.getTime transfiguration))
       "\nPentecost is " (.format formatter (.getTime pentecost))
       "\nChrist the King is " (.format formatter (.getTime (LC/ChristTheKing year)))
       ))

(defn applicableDay [liturgicalDay date]
  (let [year (.get date GregorianCalendar/YEAR)
	litYear (LC/liturgicalYear date)
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
	       (.before (doto (LC/Easter year) (.add GregorianCalendar/DATE -50))
			date) ; transfiguration (day before)
	       (.before date
			(doto (LC/Easter year) (.add GregorianCalendar/DATE 50))) ; pentecost (day after)
	       (= (doto (LC/Easter year) (.add GregorianCalendar/DATE (+ (last (first liturgicalDay)) (nth liturgicalDay 1)))) date)
	       )
	  true
	  (and (= (ffirst liturgicalDay) :Christmas)
	       (or (= (nth liturgicalDay 3) :*)
		   (= (nth liturgicalDay 3) litYear))
	       (or (.before date
			    (doto (LC/Easter year) (.add GregorianCalendar/DATE -49))) ; transfiguration
		   (.before (doto (LC/Easter year) (.add GregorianCalendar/DATE 49)) ; pentecost
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
	     ;(.setText tv (calcHolidays (.get (new GregorianCalendar) GregorianCalendar/YEAR)))
	     (on-click (view-by-id R$id/date_button)
		       (let [dp (view-by-id R$id/main_date)]
			 ;(.setText tv (calcHolidays (.getYear dp)))
			 (.setText hol (apply str (interpose "\n" (map last (filter (fn [possibleDay] (applicableDay possibleDay (new GregorianCalendar (.getYear dp) (.getMonth dp) (.getDayOfMonth dp)))) bigList)))))
			 )))
	   ))

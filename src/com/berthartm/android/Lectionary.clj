(ns com.berthartm.android.Lectionary
   (:use clj-android)
   (:require [com.berthartm.android.LectionaryCalcs :as LC])
   (:use com.berthartm.android.RevisedCommonLectionary)
   (:import (com.berthartm.android.Lectionary R$layout R$id))
   (:import (java.util GregorianCalendar)
	    (android.widget ArrayAdapter))
   )

(defn calcHolidays [year]
  (def easter (LC/Easter year))
  (def pentecost (doto (LC/Easter year) (.add GregorianCalendar/DATE 49))) ; 50th day of Easter
  (def ashWednesday (doto (LC/Easter year) (.add GregorianCalendar/DATE -46))) ; 40 days + 6 Sundays before Easter
  (def transfiguration (doto (LC/Easter year) (.add GregorianCalendar/DATE -49))) ; Sunday before Ash Wednesday
  (def formatter (java.text.SimpleDateFormat. "MMM d"))
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
		   (GregorianCalendar. (- year 1) 11 25)
		   :else ( GregorianCalendar. year 11 25))]
    (cond (and (= (:season liturgicalDay) :Date) ; todo: weekOffset and dayOffset don't make much sense as names for :Date
	       (= (:weekOffset liturgicalDay) (+ (.get date GregorianCalendar/MONTH) 1))
	       (= (:dayOffset liturgicalDay) (.get date GregorianCalendar/DAY_OF_MONTH))
	       (or (= (:year liturgicalDay) :*)
		   (= (:year liturgicalDay) litYear)))
	  true
	  (and (= (:season liturgicalDay) :Easter)
	       (or (= (:year liturgicalDay) :*)
		   (= (:year liturgicalDay) litYear))
	       (.before (doto (LC/Easter year) (.add GregorianCalendar/DATE -50))
			date) ; transfiguration (day before)
	       (.before date
			(doto (LC/Easter year) (.add GregorianCalendar/DATE 50))) ; pentecost (day after)
	       (= (doto (LC/Easter year) (.add GregorianCalendar/DATE (+ (:seasonalOffset liturgicalDay)
									 (:weekOffset liturgicalDay)))) date)
	       )
	  true
	  (and (= (:season liturgicalDay) :Christmas)
	       (or (= (:year liturgicalDay) :*)
		   (= (:year liturgicalDay) litYear))
	       (or (.before date
			    (doto (LC/Easter year) (.add GregorianCalendar/DATE -49))) ; transfiguration
		   (.before (doto (LC/Easter year) (.add GregorianCalendar/DATE 49)) ; pentecost
			    date))
	       (= (doto xmas ; should be safe to use xmas, as it's all in one doto clause
		    (.add GregorianCalendar/DATE
			  (+ (* 7 (+ (:seasonalOffset liturgicalDay)
				     (:weekOffset liturgicalDay)))
			     (cond (= (.get xmas GregorianCalendar/DAY_OF_WEEK) 1) -7
				   :else (- 1 (.get xmas GregorianCalendar/DAY_OF_WEEK))
				   ) ; find the Sunday (Prior, so 1st sunday of X can be a '1' in the declaration)
			     (:dayOffset liturgicalDay)))) ; day offset (Sunday reading, Monday reading, etc.)
		  date)
	       )
	  true
	  :else false))) ; base case, this day does not apply


(defmacro on-item-click [view handler]
  "Attach a item click listener to a view. The handler form is provided 'view as a reference to the calling view."
  `(.setOnItemClickListener ~view (proxy [android.widget.AdapterView$OnItemClickListener] []
				    (onItemClick [~'parent ~'view ~'pos ~'id] (boolean ~handler)))))

(defactivity Main
  (:create (.setContentView context R$layout/main)
	   (on-click (view-by-id R$id/date_button)
		     (let [dp (view-by-id R$id/main_date)]
		       (.setAdapter (view-by-id R$id/holiday_list)
				    (ArrayAdapter. context android.R$layout/simple_list_item_1
						   (into-array
						    (filter (fn [possibleDay]
							      (applicableDay possibleDay
									     (GregorianCalendar. (.getYear dp)
												 (.getMonth dp)
												 (.getDayOfMonth dp))))
							    bigList))))
		       ))
	   (on-item-click (view-by-id R$id/holiday_list)
			  (.setText (view-by-id R$id/notice) (str (:season (.getItemAtPosition parent pos)))))
	   ))

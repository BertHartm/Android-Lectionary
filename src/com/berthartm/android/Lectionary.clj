(ns com.berthartm.android.Lectionary
   (:use clj-android)
   (:require com.berthartm.android.LectionaryCalcs)
   (:import (com.berthartm.android.Lectionary R$layout R$id))
   )

(defn calcHolidays [year] (let [LC (new com.berthartm.android.LectionaryCalcs)]
			    (def easter (.Easter LC year))
			    (def pentecost (doto (.Easter LC year) (.add java.util.GregorianCalendar/DATE 49))) ; 50th day of Easter
			    (def ashWednesday (doto (.Easter LC year) (.add java.util.GregorianCalendar/DATE -46))) ; 40 days + 6 Sundays before Easter
			    (def transfiguration (doto (.Easter LC year) (.add java.util.GregorianCalendar/DATE -49))) ; Sunday before Ash Wednesday
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
(def Advent '(Christmas -4))
(def Epiphany '(Christmas 2))
(def AshWednesday '(Easter -46))
(def Transfiguration '(Easter -49))
(def Lent '(Easter -42))
(def Ascension '(Easter 40))
(def PentecostSunday '(Easter 49))
(def HolyTrinity '(Easter 56))
(def Pentecost '(Christmas -30))
(def ChristTheKing '(Christmas -5))

(def bigList '(
	       (:Date 1 27 :A "Awesome Day")
	       (Advent 1 :A "First Sunday of Advent")
	       (AshWednesday 0 :A "Ash Wednesday")
	       ))

(defn applicableDay [liturgicalDay date]
  (cond (and (= (first liturgicalDay) :Date)
	     (= (nth liturgicalDay 1) (+ (.get date java.util.GregorianCalendar/MONTH) 1))
	     (= (nth liturgicalDay 2) (.get date java.util.GregorianCalendar/DAY_OF_MONTH))
	     (or (= (nth liturgicalDay 3) :*) (= (nth liturgicalDay 3) (.liturgicalYear (new com.berthartm.android.LectionaryCalcs) date))))
	true
	true false))
					 

(defactivity Main
  (:create (.setContentView context R$layout/main)
	   (let [tv (view-by-id R$id/holiday_info) hol (view-by-id R$id/holiday_list)]
	     (.setText tv (calcHolidays (.get (new java.util.GregorianCalendar) java.util.GregorianCalendar/YEAR)))
	     (on-click (view-by-id R$id/date_button)
		       (let [dp (view-by-id R$id/main_date)]
			 (.setText tv (calcHolidays (.getYear dp)))
			 (.setText hol (last (first (filter (fn [x] (applicableDay x (new java.util.GregorianCalendar (.getYear dp) (.getMonth dp) (.getDayOfMonth dp)))) bigList))))
			 )
		       ))
	   ))

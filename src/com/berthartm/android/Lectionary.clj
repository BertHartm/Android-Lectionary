(ns com.berthartm.android.Lectionary
   (:use clj-android)
   (:require com.berthartm.android.LectionaryCalcs)
   (:import (com.berthartm.android.Lectionary R$layout R$id))
   )

(defactivity Main
  (:create (.setContentView context R$layout/main)
	   (let [tv (view-by-id R$id/holiday_info) GC (new java.util.GregorianCalendar) LC (new com.berthartm.android.LectionaryCalcs)]
	     (def year (- (.get GC java.util.GregorianCalendar/YEAR) 1))
	     (def easter (.Easter LC year))
	     (def pentecost (doto (.Easter LC year) (.add java.util.GregorianCalendar/DATE 49))) ; 50th day of Easter
	     (def ashWednesday (doto (.Easter LC year) (.add java.util.GregorianCalendar/DATE -46))) ; 40 days + 6 Sundays before Easter
	     (def transfiguration (doto (.Easter LC year) (.add java.util.GregorianCalendar/DATE -49))) ; Sunday before Ash Wednesday
	     (def formatter (new java.text.SimpleDateFormat "MMM d"))
	     (.setText tv (str "for " year
			       "\nEaster is " (.format formatter (.getTime easter))
			       "\nAsh Wednesday is " (.format formatter (.getTime ashWednesday))
			       "\nTransfiguration is " (.format formatter (.getTime transfiguration))
			       "\nPentecost is " (.format formatter (.getTime pentecost))
			       "\nChrist the King is " (.format formatter (.getTime (.ChristTheKing LC year)))
			       )))))

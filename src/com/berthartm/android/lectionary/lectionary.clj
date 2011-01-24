(ns com.berthartm.android.lectionary.lectionary
  (:gen-class
   :extends android.app.Activity
   :exposes-methods {onCreate superOnCreate}
   :require 'com.berthartm.android.lectionary.LectionaryCalcs))

(defn -onCreate [this #^android.os.Bundle bundle]
  (.superOnCreate this bundle)
  (.setContentView this com.berthartm.android.lectionary.R$layout/main)
  (let [tv (new android.widget.TextView this) GC (new java.util.GregorianCalendar) LC (new com.berthartm.android.lectionary.LectionaryCalcs)]
    (def year (- (.get GC java.util.GregorianCalendar/YEAR) 1))
    (def easter (.Easter LC year))
    (def pentecost (doto (.Easter LC year) (.add java.util.GregorianCalendar/DATE 49))) ; 50th day of Easter
    (def ashWednesday (doto (.Easter LC year) (.add java.util.GregorianCalendar/DATE -46))) ; 40 days + 6 Sundays before Easter
    (def transfiguration (doto (.Easter LC year) (.add java.util.GregorianCalendar/DATE -49))) ; Sunday before Ash Wednesday
    (def formatter (new java.text.SimpleDateFormat "MMM d"))
    (.setText tv (str "for " year "\nEaster is " (.format formatter (.getTime easter)) "\nAsh Wednesday is " (.format formatter (.getTime ashWednesday)) "\nTransfiguration is " (.format formatter (.getTime transfiguration)) "\nPentecost is " (.format formatter (.getTime pentecost)) "\nChrist the King is " (.format formatter (.getTime (.ChristTheKing LC year))) ))
    (.setContentView this tv))
  )
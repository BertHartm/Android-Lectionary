(ns com.berthartm.android.RevisedCommonLectionary)

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

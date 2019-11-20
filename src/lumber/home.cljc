(ns lumber.home
  (:require
   [uix.dom.alpha :as uix.dom]
   [uix.core.alpha :as uix]
   [xframe.core.alpha :as xf :refer [<sub]]

   [lumber.ui :as ui]
   [lumber.dots :as dots]
   ))


(defn nav []
  [:header.main
   ;; [:div.logo  [:img {:src "images/Logo.svg"}]]
   [:div.logo  [:span.t1 "Lumber"]]
   [:div.nav   [ui/progress]]
   [:div.email [:div.a.u
                [:span.disk-cont [:div.disk]]
                [:a.a {:href "mailto:hello@lumberdev.nyc"} "Build Something with Us"]]]])


(defn grid []
  [:div.grid

   [:section.box.dots.white-bg
    [:div.ar [dots/grid-dots]]]

   [:section.box.info
    [:div.ar
     [:header  [:h3.h3 "Who we are"]]
     [:article [:h2.h2 "We are a development firm with a global network of developers."]]
     [:footer  [:a.btn.re.black {:href "mailto:hello@lumberdev.nyc"} "Email Us"]]]]

   [:section.box.value
    [:div.ar
     [:article.black-bg [:h2.h2.white "Full-Time and Part-Time Development"]]]]

   [:section.box.work.green-gr-h
    [:div.ar
     [:header.title  [:h3.h3 "Open Source"]]
     [:header.name   [:h3.h3 "Vimsical"]]
     [:article.desc  [:h2.h2 "Vimsical"] [:p.t1 "Version control as a learning tool"]]
     [:article.image [:img {:src "./images/Vimsical.jpg"}]]
     [:footer        [:a.a.u {:on-click #(xf/dispatch [:open "https://www.youtube.com/embed/OtvK24bG_IY"])}
                      [:div.video [:span.play] "Play Video"]]
      [:div.pipe]
      [:a.a.u.link {:href "https://github.com/vimsical/vimsical" :target "_blank"}
       [:div "See Source" [:span.arrow]]]]]]

   [:section.box.value
    [:div.ar
     [:article.black-bg [:h2.h2.white "Ongoing Maintenance"]]]]

   [:section.box.quote
    [:div.ar
     [:article [:h2.h2 "The Future of Work is Remote"]]
     [dots/floating-dots 7 [0 30 100]]]]

   [:section.box.clock
    [:div.ar.yellow-bg
     [:header.title [:h3.h3 "Location"]]
     [:article.desc [:h2.h2 "New York"] [:p.t1 "United States"]]
     [ui/clock -5]]]

   ;; eyes
   [:section.box.dots.white-bg
    [:div.ar [dots/flipping-dots]]]

   [:section.box.work.purple-gr-h
    [:div.ar
     [:header.title  [:h3.h3 "In-House Project"]]
     [:header.name   [:h3.h3 "Duct"]]
     [:article.desc  [:h2.h2 "Duct"] [:p.t1 "IFTTT for developers"]]
     [:article.image [:img {:src "./images/Duct.jpg"}]]
     [:footer
      [:a.btn.re.black {:href "https://duct.cloud/" :target "_blank"}
       [:div "Go to Site"]]]]]

   [:section.box.info.black-bg
    [:div.ar
     [:header  [:h3.h3 "Clients & Partners"]]
     [:article [:ul.white (for [p (<sub [:db/partners])] ^{:key p} [:li.h2 [:a p]])]]
     [:footer  [:a.btn.re.white {:href "mailto:hello@lumberdev.nyc"} "Build Something With Us"]]]]

   [:section.box.work.peach-gr-h
    [:div.ar
     [:header  [:h3.h3 "Open Source"]]
     [:article [:h2.h2 "Zeal"] [:p.t1 "Clipboard manager meets programming environment"]]
     [:footer  [:a.a.u {:href "https://github.com/vimsical/vimsical" :target "_blank"} [:div.video "Learn More"]]
      [:div.pipe]
      [:a.a.u.link {:href "https://github.com/vimsical/vimsical" :target "_blank"}
       [:div "See Source" [:span.arrow]]]]]]

   [:section.box.value
    [:div.ar
     [:article.black-bg [:h2.h2.white "Specialized Engineers"]]]]

   [:section.box.work.green-gr-h
    [:div.ar
     [:header  [:h3.h3 "In-House Project"]]
     [:article [:h2.h2 "Juergen"] [:p.t1 "A self-shooting camera app in React Native"]]
     [:footer  [:a.btn.re.black {:href "https://github.com/lumberdev/Juergen" :target "_blank"} "See Source"]]]]

   [:section.box.dots.white-bg
    [:div.ar [dots/raining-dots]]]

   [:section.box.info
    [:div.ar
     [:header  [:h3.h3 "Need Extra Hands"]]
     [:article [:h2.h2 "Grow your team with our global talent network of developers"]]
     [:footer  [:a.btn.re.black {:href "mailto:hello@lumberdev.nyc"} "Build Something with Us"]]]]

   [:section.box.value
    [:div.ar
     [:article.black-bg [:h2.h2.white "Dedicated Teams"]]]]

   [:section.box.work.purple-gr-h
    [:div.ar
     [:header  [:h3.h3 "Auth0-clojure"]]
     [:article [:h2.h2 "auth0-clojure"] [:p.t1 "Clojure client library for the [Auth0] platform"]]
     [:footer  [:a.a.u {:href "" :target "_blank"} [:div.video ""]]
      [:a.btn.re.black {:href "https://github.com/lumberdev/auth0-clojure" :target "_blank"}
       [:div "See Source"]]]]]

   [:section.box.clock
    [:div.ar.yellow-bg
     [:header.title [:h3.h3 "Location"]]
     [:article.desc [:h2.h2 "Sofia"] [:p.t1 "Bulgaria"]]
     [ui/clock 2]]]

   [:section.box.dots.white-bg
    [:div.ar [dots/raining-dots]]]

   [:section.box.quote
    [:div.ar
     [:article [:h2.h2 "The Future of Work is Remote"]]
     [dots/floating-dots 7 [0 0 0 0]]]]

   [:section.box.info
    [:div.ar
     [:header  [:h3.h3 "Say Hello"]]
     [:article [:p.h2 "Send Us Electronic Mail"]]
     [:footer  [:div.btn.re.black "hello@lumberdev.nyc"]]]]

   ;; [:section.box.dots.white-bg
   ;;  [:div.ar [dots/glow-dots]]]

   ;; [:section.box.quote
   ;;  [:div.ar
   ;;   [:article [:h2.h2 "The Future of Work is Remote"]]
   ;;   [dots/bumping-dots 7 (rand-nth [0 0 0 30])]]]

   ;; [:section.box.dots.white-bg
   ;;  [:div.ar [dots/eye-dots [1 2]]]]

   ])

(defn footer []
  [:footer.main.t1
   [:div.left "Made in New York & Sofia"]
   [:div.center ""]
   [:div.right "All Rights Reserved"]])

(defn home []
  [:div
   [:div.cont
    [nav]
    [grid]
    [footer]]
   [ui/popup]])

(ns lumber.core
  (:require
   [uix.dom.alpha :as uix.dom]
   [uix.core.alpha :as uix]
   [lumber.ui :as ui]
   [lumber.dots :as dots]
   [xframe.core.alpha :as xf :refer [<sub]]
   #?(:cljs [cljs-bean.core :as bean])))

(xf/reg-event-db
 :db/init
 (fn [_ _]
   {:partners
    ["Casper" "Resy" "Choosy" "Patagonia" "Workframe" "Village Studio"]
    :projects
    {:vimsical {:video "https://www.youtube.com/watch?v=OtvK24bG_IY&feature=youtu.be"
                :code  "https://github.com/vimsical/vimsical"}
     :juergen  {:code  "https://github.com/lumberdev/Juergen"}}
    :open false
    }))

(def db {:partners
         ["Casper" "Resy" "Choosy" "Patagonia" "Workframe" "Village Studio"]
         :projects
         {:vimsical {:video "https://www.youtube.com/watch?v=OtvK24bG_IY&feature=youtu.be"
                     :code  "https://github.com/vimsical/vimsical"}
          :juergen  {:code  "https://github.com/lumberdev/Juergen"}}
         })

(defn nav []
  [:header.main
   ;; [:div.logo  [:img {:src "images/Logo.svg"}]]
   [:div.logo  [:span.t1 "Lumber"]]
   [:div.nav   [:div.state] [:div.rail]]
   [:div.email [:div.a.u
                [:span.disk-cont [:div.disk]]
                [:a.a {:href "mailto:hello@lumberdev.nyc"} "Build Something with Us"]]]])

(defn footer []
  [:footer.main.t1 [:div.left "Made in New York & Sofia"] [:div.center ""] [:div.right "All Rights Reserved"]])

(defn grid []
  [:div.grid
   [:section.box.info
    [:header  [:h3.h3 "Who we are"]]
    [:article [:h2.h2 "We are a development firm that works with a global network with developers."]]
    [:footer  [:div.btn.re.black "Email Us"]]]

   [:section.box.info.black-bg
    [:header  [:h3.h3 "Clients & Partners"]]
    [:article [:ul.white (for [p (-> db :partners)] ^{:keys p} [:li.h2 [:a p]])]]
    [:footer  [:div.btn.re.white "Build Something With Us"]]]

   [:section.box.info
    [:header  [:h3.h3 "Say Hello"]]
    [:article [:p.h2 "Send Us Electronic Mail"]]
    [:footer  [:div.btn.re.black "hello@lumberdev.nyc"]]]

   [:section.box.work.green-gr-h
    [:header.title  [:h3.h3 "Open Source"]]
    [:header.name   [:h3.h3 "Vimsical"]]
    [:article.desc  [:h2.h2 "Vimsical"] [:p.t1 "Version control for digital education"]]
    [:article.image [:img {:src "/images/Vimsical.jpg"}]]
    [:footer        [:a.a.u {:href "https://www.youtube.com/watch?v=OtvK24bG_IY&feature=youtu.be" :target "_blank"}
                     [:div.video [:span.play] "Play Video"]]
                     [:div.pipe]
                     [:a.a.u.link {:href "https://github.com/vimsical/vimsical" :target "_blank"}
                      [:div "See Source" [:span "\u2197"]]]]]

   [:section.box.work.peach-gr-h
    [:header  [:h3.h3 "Open Source"]]
    [:article [:h2.h2 "Zeal"] [:p.t1 "Clipboard manager meets programming environment"]]
    [:footer  [:a.a.u {:href "https://github.com/vimsical/vimsical" :target "_blank"} [:div.video "Learn More"]]
     [:div.pipe]
     [:a.a.u.link {:href "https://github.com/vimsical/vimsical" :target "_blank"}
      [:div "See Source" [:span "\u2197"]]]]]

   [:section.box.work.purple-gr-h
    [:header  [:h3.h3 "AuthO-clojure"]]
    [:article [:h2.h2 "authO-clojure"] [:p.t1 "Clojure client library for the [Auth0] platform"]]
    [:footer  [:a.a.u {:href "" :target "_blank"} [:div.video ""]]
     [:a.btn.re.black {:href "https://github.com/lumberdev/auth0-clojure" :target "_blank"}
      [:div "See Source"]]]]

   [:section.box.work.green-gr-h
    [:header  [:h3.h3 "In-House Project"]]
    [:article [:h2.h2 "Juergen"] [:p.t1 "A self-shooting camera app in React Native"]]
    [:footer  [:a.btn.re.black {:href "https://github.com/lumberdev/Juergen" :target "_blank"} "Go to site"]]]

   [:section.box.work.purple-gr-h
    [:header.title  [:h3.h3 "In-House Project"]]
    [:header.name   [:h3.h3 "Duct"]]
    [:article.desc  [:h2.h2 "Duct"] [:p.t1 "IFTTT for developers"]]
    [:article.image [:img {:src "/images/Duct.jpg"}]]
    [:footer
     [:a.btn.re.black {:href "https://github.com/lumberdev/duct" :target "_blank"}
      [:div "Go to Site"]]]]

   [:section.box.quote
    [:article [:h2.h2 "The Future of Work is Remote"]]
    [:svg
     (for [p (dots/rand-pos-seq 7 300 300)]
       [:circle {:cx (:x p)  :cy (:y p)  :r 20 :fill "#ffcc08"}])
     ]]

   [:section.box.value
    [:article.black-bg [:h2.h2.white "Full-Time and Part-Time Development"]]]

   [:section.box.value
    [:article.black-bg [:h2.h2.white "Ongoing Maintenance"]]]

   [:section.box.value
    [:article.black-bg [:h2.h2.white "Specialized Engineers"]]]

   ;; [:section.box.value
   ;;  [:article.black-bg [:h2.h2.white "Dedicated Teams"]]]


   ;; [:div.dots
    ;; [:div
    ;;  (for [p (flatten dot-matrix)]
    ;;    [:div {:style (str "top: 0;" "left: 0;" "width: 30px;" "background:" "#000;")}])
    ;;  ]
    ;; [:svg
    ;;  (for [p (flatten dot-matrix)]
    ;;    [:circle {:cx (:x p) :cy (:y p)  :r 30 :fill "#000"}])
    ;;  ]
    ;; ]
   ])

(defn home []
  [:div.cont
   [nav]
   [grid]
   [footer]
   [ui/popup]])

(defn start []
  (prn "start")

  (defonce init-db (xf/dispatch [:db/init]))

  (uix.dom/hydrate
   [home]
   (.getElementById js/document "app")))

(defn ^:export init []
;; init is called ONCE when the page loads
;; this is called in the index.html and must be exported
;; so it is available even in :advanced release builds
  (start))

(defn stop []
;; stop is called before any code is reloaded
;; this is controlled by :before-load in the config
  (prn "stop"))

(defn reload [] (prn "reload"))

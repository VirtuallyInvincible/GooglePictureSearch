# GooglePictureSearch
Android project for searching photos via Google search API. Tap a photo to watch it in full screen. Both portrait and landscape modes are
supported, even for full screen mode of pictures (not trivial - Android destroy's the Activity and recreates it, and the full screen mode
is actually a dialog).

The application is built in a manner that maximizes lose coupling. The component which displays the pictures and their titles is
completely independent of the component that enables the user to insert the search expression and initiate a new search. Feel free to
replace any of these components with your own. You can choose, for instance, to enable the user to select a search subject from a drop-
down-list and display the results in a grid, or to have a horizontally scrolling list which displays pictures as soon as the user starts
typing in. Just write your components, have them implement the defined interfaces and plug them in.

The mediator desing pattern is used for communicating between the displayer component and the input component. Feel free to replace it
with your own mediator, if necessary.

In the current implementation, scroll down the list to retrieve the next page of results. Each page contains 10 pictures. Note that Google
limits the number of queries per day, so eventually you'll start getting authentication errors, but this is completely normal.

I'm using this opportunity to demonstrate the use of state-of-the-art libraries and Android development tools (as of today - 17.12.2016).
I'm using RecyclerView as the display component. Crashlytics is used to track any usage of the app and spot crashes. A myriad of popular
libraries are integrated - Retrofit and Volley for networking, Picasso for loading pictures and displaying them directly into the
appropriate View and gson is used together with Retrofit to parse the JSON from Google API.

Note that Retrofit requires that the names of the fields in the model match exactly the names of the keys in the JSON from which their
values are obtained.

![alt tag](https://cloud.githubusercontent.com/assets/14920433/21288201/3d854f7a-c485-11e6-8b8d-f3a41d7f7347.png)
![alt tag](https://cloud.githubusercontent.com/assets/14920433/21288203/43efe1cc-c485-11e6-9035-f78396413a93.png)

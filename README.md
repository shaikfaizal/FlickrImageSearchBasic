# FlickrImageSearchBasic
Flickr Image can search and view

# How it works

Using Flickr api search image
(default - flower)

By using fab button we can search different images like kitten, ball, etc.,

After getting data from the server just load into the recycler view and cache using LRU

# LRUCache

Currently i allocated 4MB cache to the LRU and runnung it
Once 4MB is filled, they will delete old cache and added new images.

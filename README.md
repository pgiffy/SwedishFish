# SwedishFish
This was created during the 2018 summer REU at MSU.
This was made by Peter Gifford from MSU (me) and Dan Wood from Michigan Tech.

This program is the implementation of several algorithms to solve the NP-Hard problem of reconstructing RNA sequences.

There is the SwedishFish algorithm which focuses on breaking down the graph to its simplest form and taking a cut of the graph to pick edges.

There is also a Greedy Edge Removal algorithm that focuses on removing the most edges possible during edge removal.

The program in its current state will run these algorithms and Greedy Width so they can be compared.

# Datasets
The data being used is based on the Salmon and Flux papers that generate synthetic versions of RNA data for different animals.

The data can be found at here: https://cmu.app.box.com/s/p687exyr279wny7fb2rb36wifvavvu3f

This data was borrowed from an algorithm called catfish (https://github.com/Kingsford-Group/catfish). We were tasked with beating catfish, so we are using identical data sets so comparison will be unbiased.

At the top of main there is a directory and animal variable that should be changed to the appropriate location and desired animal. The options are: human,zebrafish, mouse, and salmon (salmon is significantly larger than the others).

There is also data availible for synthetc randomly generated graphs. The number of paths on these is larer than ten, so to get data from them, some of the statements throughout main need to be altered to allow for bigger number outputs.

# Presentation
A poster on this was presented at MSU (This can be found above), and a paper will most likely be written in the near future.

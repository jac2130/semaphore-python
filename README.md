##semaphore-python
================

A python wrapper for Semaphore, a Shallow Semantic Parser that identifies roles in a text.

For any questions, suggestions, or feedback, please e-mail me: jac2130@columbia.edu

#How it can be used:

The detailed description of what Semaphore is and does, i.e, a description of its current scientific uses can be found here: [http://www.mitpressjournals.org/doi/pdf/10.1162/COLI_a_00163].

Dipanjan Das, Andre Martins, Nathan Schneider, Desai Chen, & Noah A. Smith Language Technologies Institute, Carnegie Mellon University [http://www.ark.cs.cmu.edu/SEMAFOR]

Thus, I will not describe the scientific uses here, but I will simply describe how this program is used from a purely practical perspective as well as the reasons for this Python wrapper.

#To run the program on a list of files.
This is recommended because as it stands each time that the program is launced the MST parser is trained anew and that takes a while. This makes running the program on a few sentences not worth the trouble:

    from semaphore import semaphore

    frames=semaphore(files=file_paths)

where "file_paths" is a python list of file paths in the form of

    file_path=['/my_path1/text1.txt', 'my_path2/text2.txt', ...etc.]

The output is a list of python dictionaries, "frames", where frames[i]['text'] is a text string of the i's sentence and where frames[i]['fn-labels'] is a list of list of frames found in that sentence (frame labels and their associated concepts and relations).

#To run the program on a string:

	frames=semaphore("Hello World")

####Note that you can also run the program on both a string and a list of files:

    frames = semaphore("Hello World. The following will be from files", files=file_paths)

####If you run semaphore empty, it will run on a supplied example file, like so:

    frames =semaphore()

####Seperating running the semaphore program and later loading of the xml files as a list of python dictionaries:

    run_semaphore(sample=file_path)

runs semaphore on a single file with path "file_path" and stores the xml file with the frame labels. You can later load in the frames as a python object, provided that you have not run semaphore on other files or strings in the meantime:

    frames = import_semaphore()

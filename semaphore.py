
import re, sys, os


#getting the directories which are set by the user in the config file (located in the release directory).

from directories import *

def run_semaphore(semaphore=release, command='./fnParserDriver.sh', sample='../samples/sample.txt', output= '../samples/output.txt'):
    #This function produces an xml file containing the frame-net style frames.
    #"./fnParserDriver.sh" is an unwieldy bash script that I'm hoping to replace entirely with python code soon, in order to have fine
    #control of the behavior of semaphore from python. But this is still relatively low on my list of priorities.

    os.chdir(semaphore)

    os.system(command + ' ' + sample + " " + output)


def import_semaphore(xml=output):

    '''
    Takes the xml output that results from running the Semaphore program and returns a python object.
    '''
    from collections import OrderedDict
    import xmltodict
    f=open(xml, 'r').read()
    raw_dict=xmltodict.parse(f)

    #cutting the initial layers of an unwieldy xml dictionary with far too many xml tags, as we are assuming a list of sentences:
    raw_list=raw_dict[u'corpus'][u'documents'][u'document'][u'paragraphs'][u'paragraph'][u'sentences'][u'sentence']

    try: raw_text=[str(raw_list[i][u'text']) for i in range(len(raw_list))]
    except: raw_text=[raw_list['text']]

    #sometimes there are annotationSets and sometimes not:

    def annotationSets(dictionary):
        try:
            return dictionary['annotationSets']['annotationSet']
        except:
            return []

    try: raw_list=[annotationSets(raw_list[i]) for i in range(len(raw_list))] #cleaning it up further
    except: raw_list=[annotationSets(raw_list)]

    def get_frame_names(list_or_dict, keys=['@frameName']):
        #This is a function that deals with the uncertainty of whether we are getting a list or dictionary here (the xml output of semaphore is quite unwieldy)

        if not list_or_dict: #in the case that it is empty
            return []

        try:
            return [str(list_or_dict[j][key]) for j in range(len(list_or_dict)) for key in keys] #in case that it is a list
        except KeyError:
            try:
                return [str(list_or_dict[key]) for key in keys] #in case that it is a dictionary
            except:
                return [[str(list_or_dict[j][r][key]) for r in range(len(list_or_dict[j])) if raw_list[i][j][r][u'labels']!=None ] for j in range(len(list_or_dict)) for key in keys]

    frames=[get_frame_names(raw_list[i]) for i in range(len(raw_list))]

    layers=[eval(dictionary)['layer'] for dictionary in get_frame_names(raw_list[i], keys=['layers']) for i in range(len(raw_list))]

    labels=[[eval(dictionary)['label'] for dictionary in get_frame_names(layers[i], keys=['labels'])] for i in range(len(raw_list))]

    label_list=[[] for i in range(len(labels))]
    def make_label_list(labels, frames, raw_text):
        label_ls=[]
        try:
                #This try statement is executed if there is only one frame in the sentence.
            label_ls=[[frames[0]] + [labels[j][u'@name'], raw_text[eval(labels[j][u'@start']): eval(labels[j][u'@end'])+1]] for j in range(len(labels))]
        except:
            for j in range(len(labels)):
                for l in range(len(labels[j])):
                    if type(labels[j][l])==list:
                        label_ls= [frames[j]] + [[labels[j][l][r][u'@name'], raw_text[eval(labels[j][l][r][u'@start']): eval(labels[j][l][r][u'@end'])+1]] for r in range(len(labels[j][l]))]
                    else:
                        label_ls=[frames[i][j]] + [labels[i][j][l][u'@name'], raw_text[i][eval(labels[i][j][l][u'@start']): eval(labels[i][j][l][u'@end'])+1]]
        return label_ls

    for i in range(len(labels)):
        try: label_list[i].append(make_label_list(labels[i], frames[i], raw_text[i]))
        except: pass


    frame_dict={'fn-labels': label_list, 'text':raw_text}
    return frame_dict

def clean_raw_text(text, file_name=''):
    #cleans all text input and places the cleaned text in the 'samples' folder, one line at the time (as required by semaphore).

    import re
    import nltk, nltk.data

    sent_detector=nltk.data.load('tokenizers/punkt/english.pickle')

    raw_text=text
    clean_file=file_name if file_name else 'clean_text.txt'

    text=re.sub(r'-+(\n)\s*', '', raw_text)
    text=re.sub(r'(\n)+', '', text)

    text= '\n'.join([' '.join(nltk.word_tokenize(sent)) for sent in sent_detector.tokenize(text.strip())])
    open(clean_file, 'w').write(text)


def semaphore(text='', files='', semaphore=release):
    os.chdir(semaphore)

    if text:
        sample='../samples/cleaned.txt'
    if files:
        text=text+' '.join([open(f, 'r').read() for f in files])
        #I just name the newly cleaned file by the name of the first file in the file list + "_clean":
        sample='../samples/' + files[0].split('/')[-1][:-4] + '_clean.txt'

    clean_raw_text(text, file_name=sample)
    run_semaphore(semaphore=semaphore, sample=sample)

    return import_semaphore()

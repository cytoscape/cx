import requests, json, os, time, tempfile

NDEX_SEARCH = "http://www.ndexbio.org/v2/search/network?start=0&size=400"
NDEX_URL="http://ndexbio.org/v2/network/"
CyREST_IMPORT = "http://localhost:1234/cyndex2/v1/networks/cx"
# CyREST_IMPORT = "http://localhost:1234/v1/networks?format=cx&source=url"
CyREST_EXPORT = "http://localhost:1234/v1/collections/{networkSUID}.cx"


headers = {
    "Accept": "application/json",
    "Content-Type": "application/json"
}
data = {
    "searchString": ""
}

resp = requests.post(NDEX_SEARCH, headers=headers, data=json.dumps(data))
nets = {net['name']: net['externalId'] for net in resp.json()['networks']}

def downloadNDExNetwork(uuid, f):
    temp_path = f.name
    print("Downloading " + uuid + " to " + temp_path)
    CX = requests.get(NDEX_URL + uuid, headers=headers).json()
    json.dump(CX, f)
    f.close()


def getOutputDir():
    return os.path.join(os.getcwd(), 'loop_ndex_output')


def clearOutputDir():
    folder = getOutputDir()
    for the_file in os.listdir(folder):
        file_path = os.path.join(folder, the_file)
        try:
            if os.path.isfile(file_path):
                os.unlink(file_path)
            #elif os.path.isdir(file_path): shutil.rmtree(file_path)
        except Exception as e:
            print(e)

def importCxFile(path, name="Unnamed"):

    # CyREST
    # body = [{'source_location': "file://" + path}]
    # resp = requests.post(CyREST_IMPORT, headers=headers, data=json.dumps(body))
    # time.sleep(5)
    # resp = resp.json()
    # if 'errors' in resp and len(resp['errors']) > 0:
    #     raise Exception(resp['errors'])
    # print(resp)
    # suid = resp[0]['networkSUID']
    # print("Imported " + net_name + " to " + str(suid))
    # return suid

    # CyNDEx2
    body = open(path, 'r').read()
    resp = requests.post(CyREST_IMPORT, headers=headers, data=body)
    
    time.sleep(5)
    resp = resp.json()
    if 'errors' in resp and len(resp['errors']) > 0:
        raise Exception(resp['errors'])
    suid = resp['data']['suid']
    print("Imported " + net_name + " to " + str(suid))
    return suid

def exportCxNetwork(suid, f):
    resp = requests.get(CyREST_EXPORT.format(
        networkSUID=suid), headers=headers)
    CX = resp.json()
    if 'errors' in CX:
        raise Exception("Error importing CX: " + str(e))
    json.dump(CX, f)
    f.close()
    print("Exported to " + f.name)


def removeFile(path):
    if os.path.exists(path):
        os.unlink(path)
        assert not os.path.exists(path)

def roundTripNetwork(name, uuid):
    out_dir = getOutputDir()
    ndexNetwork = tempfile.NamedTemporaryFile(dir=out_dir, mode="w", prefix=uuid, suffix=".cx", delete=False)
    cyNetwork = tempfile.NamedTemporaryFile(
        dir=out_dir, mode="w", prefix="cx_export_" + uuid, suffix=".cx", delete=False)

    try:
        # Import, export, and re-import the network
        downloadNDExNetwork(uuid, ndexNetwork)
        suid1 = importCxFile(ndexNetwork.name, name=name)
        time.sleep(5)
        exportCxNetwork(suid1, cyNetwork)
        suid2 = importCxFile(cyNetwork.name, name=name + " round trip")
    except Exception as e:
        print("Error: " + str(e))
        raise Exception("Error in round trip: " + str(e))

    w = input("Continue?")
    if w != 'y':
        return False
    removeFile(ndexNetwork.name)
    removeFile(cyNetwork.name)
    return True

if __name__ == "__main__":
    clearOutputDir()
    for net_name in nets:
        uuid = nets[net_name]
        if not roundTripNetwork(net_name, uuid):
            break

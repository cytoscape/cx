import requests, json, os, time, tempfile

NDEX_SEARCH = "http://www.ndexbio.org/v2/search/network?start=0&size=400"
NDEX_URL="http://ndexbio.org/v2/network/"
# CyREST_IMPORT = "http://localhost:1234/cyndex2/v1/networks/cx"
CyREST_IMPORT = "http://localhost:1234/v1/networks?format=cx&source=url"
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

def importCxFile(path, name="Unnamed"):
    body = [{'source_location': "file://" + path}]
    resp = requests.post(CyREST_IMPORT + "&collection=" +
                         name, headers=headers, data=json.dumps(body))
    suid = resp.json()[0]['networkSUID']
    print("Imported " + net_name + " to " + str(suid))
    return suid

def exportCxNetwork(suid, f):
    resp = requests.get(CyREST_EXPORT.format(
        networkSUID=suid), headers=headers)
    json.dump(CX, f)
    f.close()
    print("Exported to " + f.name)


def removeFile(path):
    if os.path.exists(path):
        os.unlink(path)
        assert not os.path.exists(path)

def roundTripNetwork(name, uuid):

    ndexNetwork = tempfile.NamedTemporaryFile(mode="w", prefix=uuid, delete=False)
    cyNetwork = tempfile.NamedTemporaryFile(mode="w", prefix="cx_export_" + uuid, delete=False)

    try:
        # Import, export, and re-import the network
        downloadNDExNetwork(uuid, ndexNetwork)
        suid1 = importCxFile(ndexNetwork.name, name=name)
        time.sleep(5)
        exportCxNetwork(suid, cyNetwork)
        suid2 = importCxFile(cyNetwork.name, name=name + " round trip")
    except Exception as e:
        print(e)
        raise Exception("Error in round trip: " + e)
    finally:
        w = input("Delete the temp files?")
        if w != 'y':
            removeFile(ndexNetwork.name)
            removeFile(cyNetwork.name)


if __name__ == "__main__":
    for net_name in nets:
        uuid = nets[net_name]
        roundTripNetwork(net_name, uuid)
    
    
    

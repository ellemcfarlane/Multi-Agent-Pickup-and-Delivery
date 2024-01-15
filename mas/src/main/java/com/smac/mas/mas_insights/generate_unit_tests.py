# import OS module
import os

# Get the list of all files and directories
path = "Multi-Agent-Pickup-and-Delivery/mas/src/main/java/com/smac/mas/levels"
dir_list = os.listdir(path)

print("Files and directories in '", path, "' :")

for file in dir_list:
    nameExclLvl = file[0:-4]
    print(
        "\
    @Test(timeout = 180000)\
    public void Test_"
        + nameExclLvl
        + '() throws IOException {\
        TestMap("2021/'
        + nameExclLvl
        + '");\
    }\n'
    )

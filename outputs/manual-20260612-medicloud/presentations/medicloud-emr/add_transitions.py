import pathlib
import re
import shutil
import zipfile

pptx_path = pathlib.Path(r'C:\Users\masik\Downloads\MediCloud_EMR_Project_Presentation.pptx')
workspace = pathlib.Path(r'C:\Users\masik\AndroidStudioProjects\EasEBudgetV1C\outputs\manual-20260612-medicloud\presentations\medicloud-emr')
tmp_path = workspace / 'MediCloud_EMR_Project_Presentation.transitions.pptx'
transitions = [
    '<p:transition spd="med"><p:fade/></p:transition>',
    '<p:transition spd="med"><p:push dir="l"/></p:transition>',
]
slide_re = re.compile(r'ppt/slides/slide(\d+)\.xml$')
count = 0
with zipfile.ZipFile(pptx_path, 'r') as zin, zipfile.ZipFile(tmp_path, 'w', zipfile.ZIP_DEFLATED) as zout:
    for item in zin.infolist():
        data = zin.read(item.filename)
        match = slide_re.match(item.filename)
        if match:
            text = data.decode('utf-8')
            if '<p:transition' not in text and '</p:cSld>' in text:
                idx = int(match.group(1))
                text = text.replace('</p:cSld>', '</p:cSld>' + transitions[(idx - 1) % len(transitions)], 1)
                data = text.encode('utf-8')
                count += 1
        zout.writestr(item, data)
shutil.copy2(tmp_path, pptx_path)
print('transitions_added', count)
print('bytes', pptx_path.stat().st_size)

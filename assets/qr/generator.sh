#!/bin/bash

URLS="home:? \
home_with_parameter:?space_id=qz0n5cdakyl9&preview_token=e8fc39d9661c7468d0285a7ff949f7a23539dd2e686fcb7bd84dc01b392d698b&delivery_token=580d5944194846b690dd89b630a1cb98a0eef6a19b860ef71efc37ee8076ddb8&editorial_features=enabled&api=cda \
course:courses/hello-contentful \
category:courses/categories/getting-started \
lesson:courses/hello-contentful/lessons/apis \
lesson_with_preview_host:courses/hello-sdks/lessons/fetch-all-entries?space_id=zuhofcrybh4s&preview_token=683ae6c053eb0eb59d0ef701ec7f6dcb748ba0a614cb60ad2cea6e3e51963180&delivery_token=054c849d478f382dd31c92b52104fa7cb113867f80007b2f8907bc48f3e25031&editorial_features=enabled&api=cpa&host=flinkly.com \
lesson_with_default_host:courses/hello-sdks/lessons/fetch-all-entries?space_id=qz0n5cdakyl9&preview_token=e8fc39d9661c7468d0285a7ff949f7a23539dd2e686fcb7bd84dc01b392d698b&delivery_token=580d5944194846b690dd89b630a1cb98a0eef6a19b860ef71efc37ee8076ddb8&editorial_features=enabled&api=cda&host=contentful.com \
marios_lesson:courses/hello-sdks/lessons/fetch-draft-content?space_id=inqrpwvd6l1p&delivery_token=6fa7ba8c68f6b3d31865ca31a1077a36ce8fdf6c352795b36d3c01af68418924&preview_token=8bbb37f78d63d0595b423fdb91245a7f90596a07cd87282625d31934ff8c56b3&editorial_features=enabled&api=cpa \
quality_assurance_draft_course:courses/course-with-draft-and-pending-lending?space_id=jnzexv31feqf&delivery_token=7c1c321a528a25c351c1ac5f53e6ddc6bcce0712ecebec60817f53b35dd3c42b&preview_token=4310226db935f0e9b6b34fb9ce6611e2061abe1aab5297fa25bd52af5caa531a&editorial_features=enabled&api=cpa"

for code in ${URLS}; do
    OUTPUT="$(echo ${code}|cut -d':' -f 1).png"
    URL="$(echo ${code}|cut -d':' -f 2)"
    QR="the-example-app-mobile://${URL}"

    echo "${OUTPUT}: ${QR}"

    qrencode -s 10 ${QR} -o ${OUTPUT};
done

# add non 🍵 image for false positive checking
qrencode -s 10 "https://http.cat/451" -o zzz_no_tea.png

montage -background white -filter point -geometry '400!x400!+50+50' -label '%t' *.png all.bmp

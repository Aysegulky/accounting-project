function sliceSize(dataNum, dataTotal) {
    return (dataNum / dataTotal) * 360;
}

function addSlice(sliceSize, pieElement, offset, sliceID, color) {
    $(pieElement).append("<div class='slice " + sliceID + "' data-toggle='tooltip' data-placement='top'><span></span></div>");

    var sizeRotation = -179 + sliceSize;

    $("." + sliceID).css({
        "transform": "rotate(" + offset + "deg) translate3d(0,0,0)"
    });

    $("." + sliceID + " span").css({
        "transform": "rotate(" + sizeRotation + "deg) translate3d(0,0,0)",
        "background-color": color
    });
}

function iterateSlices(sliceSize, pieElement, offset, dataElement, dataCount, sliceCount, color) {
    var sliceID = "s" + dataCount + "-" + sliceCount;
    var maxSize = 179;


    if (sliceSize <= maxSize) {
        addSlice(sliceSize, pieElement, offset, sliceID, color);
    } else {
        addSlice(maxSize, pieElement, offset, sliceID, color);
        iterateSlices(sliceSize - maxSize, pieElement, offset + maxSize, dataElement, dataCount, sliceCount + 1, color);
    }
}

function createPie(dataElement, pieElement) {
    var listData = [];
    $(dataElement + " span").each(function () {
        // Ensure that negative values are treated as 0
        listData.push(Math.max(0, Number($(this).html())));
    });

    // Check if the sum of positive values is greater than zero
    var sumOfPositiveValues = listData.reduce((sum, value) => sum + value, 0);

    if (sumOfPositiveValues <= 0) {
        console.log("No positive values to create a pie chart.");
        return; // Exit the function or handle the case accordingly
    }

    var listTotal = 0;
    for (var i = 0; i < listData.length; i++) {
        listTotal += listData[i];
    }

    var offset = 0;
    var color = [
        "#5EB344",
        "#FCB72A",
        "#F8821A",
        "#FF5733",
        "#E0393E",
        "#5733FF",
        "#963D97",
        "#069CDB",
        "#ADAA40",
        "#C96D4C",
        "#978dad",
        "#ae5231",
        "#ffa2a1"
    ];

    for (var i = 0; i < listData.length; i++) {
        var size = sliceSize(listData[i], listTotal);
        iterateSlices(size, pieElement, offset, dataElement, i, 0, color[i]);
        $(dataElement + " li:nth-child(" + (i + 1) + ")").css("border-color", color[i]);
        offset += size;
    }
}

// Call the createPie function with your specific dataElement and pieElement
createPie(".pieID.legend", ".pieID.pie");


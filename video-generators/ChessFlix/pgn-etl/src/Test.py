def spiralTraverse(array):
    width = len(array)
    height = len(array[0])

    hlp = 0
    hrp = height - 1
    vup = 0
    vlp = width - 1

    output_array = []

    # while len(output_array) < height * width:
    while hlp <= hrp and vup <= vlp:
        # traverse perimiter
        for i in range(hlp, hrp + 1):
            output_array.append(array[vup][i])
        for i in range(vup + 1, vlp + 1):
            output_array.append(array[i][hrp])
        for i in reversed(range(hlp, hrp)):
            if hlp == hrp:
                break
            output_array.append(array[vlp][i])
        for i in reversed(range(vup + 1, vlp)):
            if vlp == vup:
                break
            output_array.append(array[i][hlp])

        hlp += 1
        hrp -= 1
        vup += 1
        vlp -= 1

    return output_array


print(spiralTraverse([
    [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
    [20, 19, 18, 17, 16, 15, 14, 13, 12, 11]
  ]))

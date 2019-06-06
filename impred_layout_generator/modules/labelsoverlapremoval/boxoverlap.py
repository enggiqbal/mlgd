import math

def do_overlap(x1, y1, w1, h1, x2, y2, w2, h2):

    x_min_1=x1-(w1/2)
    y_min_1=y1-(h1/2)
    x_max_1=x1+(w1/2)
    y_max_1=y1+(h1/2)

    x_min_2=x2-(w2/2)
    y_min_2=y2-(h2/2)
    x_max_2=x2+(w2/2)
    y_max_2=y2+(h2/2)


    if(x_max_1 <= x_min_2 or x_max_2 <= x_min_1 or
       y_max_1 <= y_min_2 or y_max_2 <= y_min_1):
        # print("No Overlap")
        overlap = {'w': 0, 'h': 0, 'a':0, 'u':-1, 'v':-1}

        return overlap

    l1=(x_min_1, y_min_1)
    l2=(x_min_2, y_min_2)

    r1=(x_max_1, y_max_1)
    r2=(x_max_2, y_max_2)

    area_1=w1*h1
    area_2=w2*h2

    # print(area_1)
    # print(area_2)

    width_overlap = (min(x_max_1, x_max_2)-max(x_min_1, x_min_2))
    height_overlap = (min(y_max_1, y_max_2)-max(y_min_1, y_min_2))

    areaI = width_overlap*height_overlap
    # print(areaI)

    total_area = area_1 + area_2 - areaI
    # print(total_area)

    overlap = {"w": width_overlap, "h": height_overlap, "a":areaI}

    return overlap

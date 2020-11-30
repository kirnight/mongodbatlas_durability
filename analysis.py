#! /usr/bin/env python3
from datetime import datetime

docs = {}
errored_writes = {}

ops = []

state = 0

errors = set()
docs_missing_writes = set()
unack_writes = set()

filename = "result.txt"
f = open(filename, "r")

stats_line = f.readline().strip().split('  ')

fail_time = datetime.strptime(stats_line[0], '%Y-%m-%dT%H:%M:%S.%f')
fix_time = datetime.strptime(stats_line[1], '%Y-%m-%dT%H:%M:%S.%f')

print(fail_time, fix_time)
while True:
    try:
        text = f.readline().strip()
    except EOFError:
        print("end of file")
        break

    if text == '':
        break

    fields = text.split(' ')

    try:
        time = datetime.strptime(fields[2], '%Y-%m-%dT%H:%M:%S.%f')
    except ValueError:
        try:
            time = datetime.strptime(fields[2], '%Y-%m-%dT%H:%M:%S')
        except ValueError:
            time = datetime.strptime(fields[2], '%Y-%m-%dT%H:%M')

    if (time > fix_time):
        state = 2

    elif (time > fail_time):
        state = 1

    op = fields[0]
    err = fields[-1]

    if err == "true":
        id, time, val, duration, err = fields[1:]
        val = int(val)
        errors.add((op, id, val, state))
        if op in {"W", "U"}:
            if id not in errored_writes:
                errored_writes[id] = []
            errored_writes[id].insert(0, (val, state))
    else:

        if op == "R":
            doc, time, actual, duration, err = fields[1:]
            expected = docs[doc][0]
            actual = int(actual)
            duration = float(duration.strip("PTS"))
            try:
                time = datetime.strptime(time, '%Y-%m-%dT%H:%M:%S.%f')
            except ValueError:
                try:
                    time = datetime.strptime(time, '%Y-%m-%dT%H:%M:%S')
                except ValueError:
                    time = datetime.strptime(time, '%Y-%m-%dT%H:%M')

            ops.append(("R", doc, duration, state))
            if actual != expected:
                if doc in errored_writes:
                    for v,s in errored_writes[doc]:
                        if actual == v:
                            docs[doc] = (v,s)
                            break
                    del errored_writes[doc]
                    unack_writes.add((doc, *docs[doc]))
                else:
                    docs_missing_writes.add((doc, *docs[doc]))

        if op == "U":
            doc, time, val, duration, err = fields[1:]
            val = int(val)
            duration = float(duration.strip("PTS"))
            try:
                time = datetime.strptime(time, '%Y-%m-%dT%H:%M:%S.%f')
            except ValueError:
                try:
                    time = datetime.strptime(time, '%Y-%m-%dT%H:%M:%S')
                except ValueError:
                    time = datetime.strptime(time, '%Y-%m-%dT%H:%M')
            ops.append(("U", doc, duration, state))

            docs[doc] = (val, state)

        if op == "W":
            doc, time, val, duration, err = fields[1:]
            val = int(val)
            duration = float(duration.strip("PTS"))
            try:
                time = datetime.strptime(time, '%Y-%m-%dT%H:%M:%S.%f')
            except ValueError:
                try:
                    time = datetime.strptime(time, '%Y-%m-%dT%H:%M:%S')
                except ValueError:
                    time = datetime.strptime(time, '%Y-%m-%dT%H:%M')
            ops.append(("W", doc, duration, state))
            docs[doc] = (val, state)




print("Total ops:", len(ops))
print("Errors:", len(errors))
print("Missing Writes:", len(docs_missing_writes))
print()
print("Missing on:", len({d[0] for d in docs_missing_writes}), "documents")

print("Unconfirmed writes:", len(unack_writes))
print("Unconfirmed on:", len({d[0] for d in unack_writes}), "documents")

print("-----")

for i in range(3):
    if i == 0:
        print("Normal")
    if i == 1:
        print("Failure")
    if i == 2:
        print("Recovery")

    errors_seg = [e for e in errors if e[3] == i]
    miss_seg = [d for d in docs_missing_writes if d[2] == i]
    unack_seg = [u for u in unack_writes if u[2] == i]

    print("Ops:", len([ o for o in ops if o[-1] == i ]))
    print("Errors: ", len(errors_seg))
    print("Missing: ", len(miss_seg))
    print("Unacknowledged: ", len(unack_seg))
    print("-----")

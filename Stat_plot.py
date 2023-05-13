import matplotlib.pyplot as plt

# Open the text file and read its contents
with open('camera_statistics.txt', 'r') as f:
    lines = f.readlines()

# Parse the data into lists of timestamps and throughput values
timestamps = []
throughputs = []
for line in lines:
    values = line.strip().split(';')
    timestamps.append(int(values[1]))
    throughputs.append(float(values[0]))

# Plot the data
plt.plot(timestamps, throughputs)
plt.title('Camera throughput over time')
plt.xlabel('Timestamp')
plt.ylabel('Throughput (bytes/s)')
plt.show()

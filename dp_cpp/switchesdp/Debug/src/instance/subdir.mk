################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../src/instance/arc.cpp \
../src/instance/instance.cpp \
../src/instance/node.cpp 

OBJS += \
./src/instance/arc.o \
./src/instance/instance.o \
./src/instance/node.o 

CPP_DEPS += \
./src/instance/arc.d \
./src/instance/instance.d \
./src/instance/node.d 


# Each subdirectory must supply rules for building sources it contributes
src/instance/%.o: ../src/instance/%.cpp src/instance/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '



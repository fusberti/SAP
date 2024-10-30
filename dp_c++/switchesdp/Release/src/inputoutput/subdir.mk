################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../src/inputoutput/read.cpp \
../src/inputoutput/readinstance.cpp 

CPP_DEPS += \
./src/inputoutput/read.d \
./src/inputoutput/readinstance.d 

OBJS += \
./src/inputoutput/read.o \
./src/inputoutput/readinstance.o 


# Each subdirectory must supply rules for building sources it contributes
src/inputoutput/%.o: ../src/inputoutput/%.cpp src/inputoutput/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


clean: clean-src-2f-inputoutput

clean-src-2f-inputoutput:
	-$(RM) ./src/inputoutput/read.d ./src/inputoutput/read.o ./src/inputoutput/readinstance.d ./src/inputoutput/readinstance.o

.PHONY: clean-src-2f-inputoutput

